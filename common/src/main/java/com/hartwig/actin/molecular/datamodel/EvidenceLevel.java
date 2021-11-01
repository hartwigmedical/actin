package com.hartwig.actin.molecular.datamodel;

import org.jetbrains.annotations.NotNull;

public enum EvidenceLevel {
    A,
    B,
    C,
    D;

    public boolean isBetterOrEqual(@NotNull EvidenceLevel levelToCompare) {
        return this.ordinal() <= levelToCompare.ordinal();
    }
}
