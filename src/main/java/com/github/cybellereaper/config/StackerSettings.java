package com.github.cybellereaper.config;

import com.github.cybellereaper.stack.chance.MergeMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;

public record StackerSettings(
        double mergeRadius,
        long mergeIntervalTicks,
        int maxStackSize,
        MergeMode mergeMode,
        double mergeChance,
        double extraHealthPerMob,
        double extraDamagePerMob,
        double extraKnockbackResistancePerMob,
        double scaleStep,
        double maxScale,
        int maxSlimeSize,
        double maxAppliedHealth,
        boolean multiplyDrops,
        boolean multiplyExperience,
        Set<EntityType> blacklist
) {
    public static StackerSettings from(FileConfiguration config) {
        var blacklist = EnumSet.noneOf(EntityType.class);

        for (String rawType : config.getStringList("blacklist")) {
            parseEntityType(rawType).ifPresent(blacklist::add);
        }

        return new StackerSettings(
                config.getDouble("merge-radius", 8.0D),
                config.getLong("merge-interval-ticks", 100L),
                Math.max(2, config.getInt("max-stack-size", 64)),
                MergeMode.fromString(config.getString("merge-mode", "ALWAYS")),
                clampChance(config.getDouble("merge-chance", 0.35D)),
                Math.max(0.0D, config.getDouble("extra-health-per-mob", 0.35D)),
                Math.max(0.0D, config.getDouble("extra-damage-per-mob", 0.10D)),
                Math.max(0.0D, config.getDouble("extra-knockback-resistance-per-mob", 0.04D)),
                Math.max(0.0D, config.getDouble("scale-step", 0.22D)),
                Math.max(1.0D, config.getDouble("max-scale", 3.0D)),
                Math.max(1, config.getInt("max-slime-size", 10)),
                Math.max(1.0D, config.getDouble("max-applied-health", 1024.0D)),
                config.getBoolean("multiply-drops", true),
                config.getBoolean("multiply-experience", true),
                Collections.unmodifiableSet(blacklist)
        );
    }

    private static double clampChance(double chance) {
        return Math.clamp(chance, 0.0D, 1.0D);
    }

    private static Optional<EntityType> parseEntityType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(EntityType.valueOf(rawType.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public boolean isBlacklisted(EntityType type) {
        return blacklist.contains(type);
    }
}
