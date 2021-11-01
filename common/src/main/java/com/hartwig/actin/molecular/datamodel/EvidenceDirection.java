package com.hartwig.actin.molecular.datamodel;

public enum EvidenceDirection {
    RESPONSIVE(true),
    PREDICTED_RESPONSIVE(true),
    RESISTANT(false),
    PREDICTED_RESISTANT(false);

    private final boolean isResponsive;

    EvidenceDirection(final boolean isResponsive) {
        this.isResponsive = isResponsive;
    }

    public boolean isResponsive() {
        return isResponsive;
    }
}
