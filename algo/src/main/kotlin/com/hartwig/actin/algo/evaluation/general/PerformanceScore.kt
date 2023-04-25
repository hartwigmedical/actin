package com.hartwig.actin.algo.evaluation.general;

import org.jetbrains.annotations.NotNull;

enum PerformanceScore {
    LANSKY,
    KARNOFSKY;

    @NotNull
    public String display() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
}
