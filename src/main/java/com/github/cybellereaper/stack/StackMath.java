package com.github.cybellereaper.stack;


public final class StackMath {
    public double healthMultiplier(int stackCount, double extraHealthPerMob) {
        return 1.0D + Math.max(0, stackCount - 1) * extraHealthPerMob;
    }

    public double damageMultiplier(int stackCount, double extraDamagePerMob) {
        return 1.0D + Math.max(0, stackCount - 1) * extraDamagePerMob;
    }

    public double knockbackResistance(double baseValue, int stackCount, double extraPerMob) {
        return Math.min(1.0D, baseValue + Math.max(0, stackCount - 1) * extraPerMob);
    }

    public double scale(double baseScale, int stackCount, double scaleStep, double maxScale) {
        if (stackCount <= 1) return baseScale;
        double scaledValue = baseScale + Math.log1p(stackCount - 1) * scaleStep;
        return Math.min(maxScale, scaledValue);
    }

    public int slimeSize(int baseSize, int stackCount, int maxSize) {
        if (stackCount <= 1) return baseSize;
        int growth = (int) Math.floor(Math.log(stackCount) / Math.log(2.0D));
        return Math.clamp(maxSize, 1, baseSize + growth);
    }
}
