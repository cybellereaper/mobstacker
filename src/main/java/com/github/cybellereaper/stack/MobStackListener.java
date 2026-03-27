package com.github.cybellereaper.stack;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MobStackListener implements Listener {
    private final JavaPlugin plugin;
    private final MobStackService mobStackService;

    public MobStackListener(JavaPlugin plugin, MobStackService mobStackService) {
        this.plugin = plugin;
        this.mobStackService = mobStackService;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        var entity = event.getEntity();
        Bukkit.getScheduler().runTask(plugin, () -> mobStackService.tryMergeAround(entity));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        mobStackService.handleDeath(event);
    }
}
