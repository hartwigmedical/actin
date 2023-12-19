package com.hartwig.actin.molecular.datamodel.driver;

public enum CopyNumberType {
    FULL_GAIN(true, false),
    PARTIAL_GAIN(true, false),
    LOSS(false, true),
    NONE(false, false);

    private final boolean isGain;
    private final boolean isLoss;

    CopyNumberType(final boolean isGain, final boolean isLoss) {
        this.isGain = isGain;
        this.isLoss = isLoss;
    }

    public boolean isGain() {
        return isGain;
    }

    public boolean isLoss() {
        return isLoss;
    }
}
