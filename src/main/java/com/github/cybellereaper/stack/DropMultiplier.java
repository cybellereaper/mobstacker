package com.github.cybellereaper.stack;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropMultiplier {

    private final StackPartitioner partitioner = new StackPartitioner();

    public List<ItemStack> multiply(List<ItemStack> originalDrops, int multiplier) {
        if (multiplier <= 1) {
            return new ArrayList<>(originalDrops);
        }

        var multipliedDrops = new ArrayList<ItemStack>();

        for (ItemStack drop : originalDrops) {
            if (drop == null || drop.getAmount() <= 0) {
                continue;
            }

            int totalAmount = drop.getAmount() * multiplier;
            for (int amount : partitioner.partition(totalAmount, drop.getMaxStackSize())) {
                ItemStack clone = drop.clone();
                clone.setAmount(amount);
                multipliedDrops.add(clone);
            }
        }

        return multipliedDrops;
    }
}
