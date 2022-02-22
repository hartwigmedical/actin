package com.hartwig.actin.molecular.orange.datamodel;

public enum EvidenceDirection {
    RESPONSIVE,
    PREDICTED_RESPONSIVE,
    RESISTANT,
    PREDICTED_RESISTANT;

    public boolean isPredicted() {
        return this == PREDICTED_RESPONSIVE || this == PREDICTED_RESISTANT;
    }

    public boolean isResponsive() {
        return this == RESPONSIVE || this == PREDICTED_RESPONSIVE;
    }

    public boolean isResistant() {
        return this == RESISTANT || this == PREDICTED_RESISTANT;
    }
}
