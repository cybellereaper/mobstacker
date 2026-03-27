package com.github.cybellereaper.stack.chance;


public final class ConfigurableMergeDecider implements MergeDecider {

    private final MergeMode mergeMode;
    private final double mergeChance;
    private final RandomSource randomSource;

    public ConfigurableMergeDecider(MergeMode mergeMode, double mergeChance, RandomSource randomSource) {
        this.mergeMode = mergeMode;
        this.mergeChance = Math.clamp(mergeChance, 0.0D, 1.0D);
        this.randomSource = randomSource;
    }

    @Override
    public boolean shouldMerge() {
        return switch (mergeMode) {
            case ALWAYS -> true;
            case RANDOM_CHANCE -> randomSource.nextDouble() < mergeChance;
        };
    }
}
