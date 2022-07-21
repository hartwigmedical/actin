package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.jetbrains.annotations.NotNull;

final class TumorTypeEvaluationFunctions {

    private TumorTypeEvaluationFunctions() {
    }

    public static boolean hasTumorWithType(@NotNull TumorDetails tumor, @NotNull Set<String> validTypes) {
        String primaryTumorType = tumor.primaryTumorType();
        String lowerCaseType = primaryTumorType != null ? primaryTumorType.toLowerCase() : null;

        String primaryTumorSubType = tumor.primaryTumorSubType();
        String lowerCaseSubType = primaryTumorSubType != null ? primaryTumorSubType.toLowerCase() : null;

        for (String validType : validTypes) {
            if (lowerCaseType != null) {
                if (lowerCaseType.contains(validType.toLowerCase())) {
                    return true;
                }
            }

            if (lowerCaseSubType != null) {
                if (lowerCaseSubType.contains(validType.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasTumorWithDetails(@NotNull TumorDetails tumor, @NotNull Set<String> validDetails) {
        String primaryTumorExtraDetails = tumor.primaryTumorExtraDetails();
        if (primaryTumorExtraDetails != null) {
            String lowerCaseDetails = primaryTumorExtraDetails.toLowerCase();
            for (String validDetail : validDetails) {
                if (lowerCaseDetails.contains(validDetail.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }
}
