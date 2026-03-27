package com.github.cybellereaper.stack.chance;

import java.util.Locale;

public enum MergeMode {
    ALWAYS,
    RANDOM_CHANCE;

    public static MergeMode fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALWAYS;
        }

        try {
            return MergeMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return ALWAYS;
        }
    }
}
