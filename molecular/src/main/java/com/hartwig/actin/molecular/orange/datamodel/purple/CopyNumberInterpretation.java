package com.hartwig.actin.molecular.orange.datamodel.purple;

public enum CopyNumberInterpretation {
    FULL_GAIN(true, false),
    PARTIAL_GAIN(true, false),
    FULL_LOSS(false, true),
    PARTIAL_LOSS(false, true);

    private final boolean isGain;
    private final boolean isLoss;

    CopyNumberInterpretation(final boolean isGain, final boolean isLoss) {
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
