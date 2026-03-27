package com.github.cybellereaper.stack;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public record StackKeys(
        NamespacedKey stackCount,
        NamespacedKey baseMaxHealth,
        NamespacedKey baseAttackDamage,
        NamespacedKey baseKnockbackResistance,
        NamespacedKey baseScale,
        NamespacedKey baseSlimeSize
) {
    public static StackKeys create(JavaPlugin plugin) {
        return new StackKeys(
                new NamespacedKey(plugin, "stack_count"),
                new NamespacedKey(plugin, "base_max_health"),
                new NamespacedKey(plugin, "base_attack_damage"),
                new NamespacedKey(plugin, "base_knockback_resistance"),
                new NamespacedKey(plugin, "base_scale"),
                new NamespacedKey(plugin, "base_slime_size")
        );
    }
}
