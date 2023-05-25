package com.hartwig.actin.clinical.datamodel.treatment.history;

import com.hartwig.actin.util.ApplicationConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TreatmentResponse {
    PROGRESSIVE_DISEASE,
    STABLE_DISEASE,
    MIXED,
    PARTIAL_REMISSION,
    COMPLETE_REMISSION,
    REMISSION;

    @Nullable
    public static TreatmentResponse createFromString(@NotNull String input) {
        switch (input.toUpperCase(ApplicationConfig.LOCALE)) {
            case "PD":
                return PROGRESSIVE_DISEASE;
            case "SD":
                return STABLE_DISEASE;
            case "MIXED":
                return MIXED;
            case "PR":
                return PARTIAL_REMISSION;
            case "CR":  // not seen in existing curation
                return COMPLETE_REMISSION;
            case "REMISSION":
                return REMISSION;
            default:
                return null;
        }
    }
}
