package com.github.cybellereaper.stack;

import com.github.cybellereaper.config.StackerSettings;
import com.github.cybellereaper.stack.chance.MergeDecider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class MobStackService {
    private final StackerSettings settings;
    private final StackKeys keys;
    private final MergeDecider mergeDecider;
    private final StackMath math = new StackMath();
    private final DropMultiplier dropMultiplier = new DropMultiplier();

    public MobStackService(StackerSettings settings, StackKeys keys, MergeDecider mergeDecider) {
        this.settings = settings;
        this.keys = keys;
        this.mergeDecider = mergeDecider;
    }

    public void mergeAllWorlds() {
        for (World world : Bukkit.getWorlds()) {
            var entities = new ArrayList<>(world.getLivingEntities());
            for (LivingEntity entity : entities) {
                tryMergeAround(entity);
            }
        }
    }

    public void tryMergeAround(LivingEntity leader) {
        if (!isEligible(leader)) return;
        var currentLeaderCount = getStackCount(leader);

        for (Entity nearby : leader.getNearbyEntities(
                settings.mergeRadius(),
                settings.mergeRadius(),
                settings.mergeRadius()
        )) {
            if (!(nearby instanceof LivingEntity candidate)
                    || !canMerge(leader, candidate)
                    || !mergeDecider.shouldMerge()) continue;

            int candidateCount = getStackCount(candidate);
            int capacity = settings.maxStackSize() - currentLeaderCount;
            if (capacity <= 0) break;

            int absorbedCount = Math.min(capacity, candidateCount);
            if (absorbedCount <= 0) continue;

            var mergedHealthRatio = mergedHealthRatio(leader, currentLeaderCount, candidate, absorbedCount);

            currentLeaderCount += absorbedCount;
            setStackCount(leader, currentLeaderCount);
            applyStackState(leader, currentLeaderCount, mergedHealthRatio);

            var candidateRemaining = candidateCount - absorbedCount;
            if (candidateRemaining == 0) {
                candidate.remove();
            } else {
                setStackCount(candidate, candidateRemaining);
                applyStackState(candidate, candidateRemaining, currentHealthRatio(candidate));
            }
        }
    }

    public void handleDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        int stackCount = getStackCount(entity);
        if (stackCount <= 1) return;
        if (settings.multiplyExperience()) event.setDroppedExp(event.getDroppedExp() * stackCount);
        if (settings.multiplyDrops()) {
            var multipliedDrops = dropMultiplier.multiply(event.getDrops(), stackCount);
            event.getDrops().clear();
            event.getDrops().addAll(multipliedDrops);
        }
    }

    private boolean canMerge(LivingEntity leader, LivingEntity candidate) {
        return leader.isValid()
                && candidate.isValid()
                && !leader.isDead()
                && !candidate.isDead()
                && !leader.getUniqueId().equals(candidate.getUniqueId())
                && leader.getWorld().equals(candidate.getWorld())
                && leader.getType() == candidate.getType()
                && isEligible(leader)
                && isEligible(candidate)
                && sameAgeState(leader, candidate);
    }

    private boolean isEligible(LivingEntity entity) {
        if (!(entity instanceof Mob leashable) ||
                !entity.isValid() || entity.isDead()
                || settings.isBlacklisted(entity.getType())
                || !entity.getPassengers().isEmpty()
                || entity.isInsideVehicle()
                || entity instanceof Tameable tameable && tameable.isTamed()
                || leashable.isLeashed())
            return false;

        return entity.customName() == null || getStackCount(entity) > 1;
    }

    private boolean sameAgeState(LivingEntity first, LivingEntity second) {
        if (first instanceof Ageable firstAgeable && second instanceof Ageable secondAgeable)
            return firstAgeable.isAdult() == secondAgeable.isAdult();

        return true;
    }

    private void applyStackState(LivingEntity entity, int stackCount, double desiredHealthRatio) {
        double baseMaxHealth = readOrStoreBaseAttribute(entity, keys.baseMaxHealth(), Attribute.MAX_HEALTH, 20.0D);
        double baseAttackDamage = readOrStoreBaseAttribute(entity, keys.baseAttackDamage(), Attribute.ATTACK_DAMAGE, 2.0D);
        double baseKnockbackResistance = readOrStoreBaseAttribute(
                entity,
                keys.baseKnockbackResistance(),
                Attribute.KNOCKBACK_RESISTANCE,
                0.0D
        );

        if (entity instanceof Slime slime) {
            int baseSlimeSize = readOrStoreBaseSlimeSize(slime);
            slime.setSize(math.slimeSize(baseSlimeSize, stackCount, settings.maxSlimeSize()));
        } else {
            double baseScale = readOrStoreBaseAttribute(entity, keys.baseScale(), Attribute.SCALE, 1.0D);
            setAttributeBaseValue(
                    entity,
                    Attribute.SCALE,
                    math.scale(baseScale, stackCount, settings.scaleStep(), settings.maxScale())
            );
        }

        double requestedMaxHealth = baseMaxHealth * math.healthMultiplier(stackCount, settings.extraHealthPerMob());
        double appliedMaxHealth = Math.min(requestedMaxHealth, settings.maxAppliedHealth());

        double requestedAttackDamage = baseAttackDamage * math.damageMultiplier(stackCount, settings.extraDamagePerMob());
        double requestedKnockbackResistance = math.knockbackResistance(
                baseKnockbackResistance,
                stackCount,
                settings.extraKnockbackResistancePerMob()
        );

        setAttributeBaseValue(entity, Attribute.MAX_HEALTH, appliedMaxHealth);
        setAttributeBaseValue(entity, Attribute.ATTACK_DAMAGE, requestedAttackDamage);
        setAttributeBaseValue(entity, Attribute.KNOCKBACK_RESISTANCE, requestedKnockbackResistance);

        double effectiveMaxHealth = readEffectiveAttributeValue(entity, appliedMaxHealth);
        double clampedHealthRatio = Math.clamp(desiredHealthRatio, 0.05D, 1.0D);
        double safeHealth = Math.clamp(effectiveMaxHealth, 0.1D, effectiveMaxHealth * clampedHealthRatio);

        entity.setHealth(safeHealth);
        updateDisplay(entity, stackCount);
    }

    private double mergedHealthRatio(LivingEntity leader, int leaderCount, LivingEntity candidate, int absorbedCount) {
        double leaderHealthUnits = currentHealthRatio(leader) * leaderCount;
        double candidateHealthUnits = currentHealthRatio(candidate) * absorbedCount;
        double totalUnits = leaderHealthUnits + candidateHealthUnits;
        int totalCount = leaderCount + absorbedCount;
        return totalCount <= 0 ? 1.0D : totalUnits / totalCount;
    }

    private double currentHealthRatio(LivingEntity entity) {
        AttributeInstance maxHealthAttribute = entity.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttribute != null ? maxHealthAttribute.getValue() : entity.getHealth();
        return maxHealth <= 0.0D ? 1.0D : Math.clamp(entity.getHealth() / maxHealth, 0.0D, 1.0D);
    }

    private double readEffectiveAttributeValue(LivingEntity entity, double fallback) {
        AttributeInstance instance = entity.getAttribute(Attribute.MAX_HEALTH);
        if (instance == null) return fallback;
        double value = instance.getValue();
        return !Double.isFinite(value) || value <= 0.0D ? fallback : value;
    }

    private void updateDisplay(LivingEntity entity, int stackCount) {
        if (stackCount <= 1) {
            entity.customName(null);
            entity.setCustomNameVisible(false);
            return;
        }

        String label = humanReadableName(entity.getType()) + " x " + stackCount;
        entity.customName(Component.text(label, NamedTextColor.GOLD));
        entity.setCustomNameVisible(true);
    }

    private String humanReadableName(EntityType type) {
        return Arrays.stream(type.name().toLowerCase(Locale.ROOT).split("_"))
                .map(this::capitalize)
                .collect(Collectors.joining(" "));
    }

    private String capitalize(String input) {
        return input.isBlank() ? input : Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    private int getStackCount(LivingEntity entity) {
        Integer storedCount = entity.getPersistentDataContainer().get(keys.stackCount(), PersistentDataType.INTEGER);
        return storedCount != null ? Math.max(1, storedCount) : 1;
    }

    private void setStackCount(LivingEntity entity, int stackCount) {
        var container = entity.getPersistentDataContainer();
        if (stackCount <= 1) {
            container.remove(keys.stackCount());
            return;
        }
        container.set(keys.stackCount(), PersistentDataType.INTEGER, stackCount);
    }

    private double readOrStoreBaseAttribute(
            LivingEntity entity,
            org.bukkit.NamespacedKey key,
            Attribute attribute,
            double fallback
    ) {
        var container = entity.getPersistentDataContainer();
        var storedValue = container.get(key, PersistentDataType.DOUBLE);
        if (storedValue != null) return storedValue;
        var instance = entity.getAttribute(attribute);
        double baseValue = instance != null ? instance.getBaseValue() : fallback;
        container.set(key, PersistentDataType.DOUBLE, baseValue);
        return baseValue;
    }

    private int readOrStoreBaseSlimeSize(Slime slime) {
        var container = slime.getPersistentDataContainer();
        var storedValue = container.get(keys.baseSlimeSize(), PersistentDataType.INTEGER);
        if (storedValue != null) return storedValue;
        var baseSize = slime.getSize();
        container.set(keys.baseSlimeSize(), PersistentDataType.INTEGER, baseSize);
        return baseSize;
    }

    private void setAttributeBaseValue(LivingEntity entity, Attribute attribute, double value) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }
}
