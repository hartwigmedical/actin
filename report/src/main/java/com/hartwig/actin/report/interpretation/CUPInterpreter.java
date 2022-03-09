package com.hartwig.actin.report.interpretation;

import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.jetbrains.annotations.NotNull;

public final class CUPInterpreter {

    static final String CUP_LOCATION = "Unknown";
    static final String CUP_SUB_LOCATION = "CUP";

    private CUPInterpreter() {
    }

    public static boolean isCUP(@NotNull TumorDetails tumor) {
        String location = tumor.primaryTumorLocation();
        String subLocation = tumor.primaryTumorSubLocation();

        return (location != null && subLocation != null && location.equals(CUP_LOCATION) && subLocation.equals(CUP_SUB_LOCATION));
    }
}
