package com.github.cybellereaper;

import com.github.cybellereaper.config.StackerSettings;
import com.github.cybellereaper.stack.MobStackListener;
import com.github.cybellereaper.stack.MobStackService;
import com.github.cybellereaper.stack.StackKeys;
import com.github.cybellereaper.stack.chance.ConfigurableMergeDecider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public final class MobStacker extends JavaPlugin {
    private BukkitTask mergeTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var settings = StackerSettings.from(getConfig());
        var keys = StackKeys.create(this);
        var mergeDecider = new ConfigurableMergeDecider(
                settings.mergeMode(),
                settings.mergeChance(),
                () -> ThreadLocalRandom.current().nextDouble()
        );
        var mobStackService = new MobStackService(settings, keys, mergeDecider);

        Bukkit.getPluginManager().registerEvents(new MobStackListener(this, mobStackService), this);

        mergeTask = Bukkit.getScheduler().runTaskTimer(
                this,
                mobStackService::mergeAllWorlds,
                settings.mergeIntervalTicks(),
                settings.mergeIntervalTicks()
        );

        getLogger().info("MobStacker enabled. Mode=" + settings.mergeMode());
    }

    @Override
    public void onDisable() {
        if (mergeTask != null) {
            mergeTask.cancel();
        }
    }
}