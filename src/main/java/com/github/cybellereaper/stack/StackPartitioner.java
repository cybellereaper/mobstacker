package com.github.cybellereaper.stack;


import java.util.ArrayList;
import java.util.List;

public final class StackPartitioner {

    public List<Integer> partition(int totalAmount, int maxStackSize) {
        if (maxStackSize <= 0) {
            throw new IllegalArgumentException("maxStackSize must be greater than zero");
        }

        var partitions = new ArrayList<Integer>();
        int remaining = Math.max(0, totalAmount);

        while (remaining > 0) {
            int amount = Math.min(maxStackSize, remaining);
            partitions.add(amount);
            remaining -= amount;
        }

        return partitions;
    }
}
