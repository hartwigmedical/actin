package com.hartwig.actin.clinical.datamodel.treatment.history;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TreatmentResponse {
    PROGRESSIVE_DISEASE,
    STABLE_DISEASE,
    MIXED,
    PARTIAL_RESPONSE,
    COMPLETE_RESPONSE,
    REMISSION;

    @Nullable
    public static TreatmentResponse createFromString(@NotNull String input) {
        switch (input.toUpperCase()) {
            case "PD":
                return PROGRESSIVE_DISEASE;
            case "SD":
                return STABLE_DISEASE;
            case "MIXED":
                return MIXED;
            case "PR":
                return PARTIAL_RESPONSE;
            case "CR":  // not seen in existing curation
                return COMPLETE_RESPONSE;
            case "REMISSION":
                return REMISSION;
            default:
                return null;
        }
    }
}
