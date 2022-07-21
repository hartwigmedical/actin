package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.doid.DoidModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

final class DoidEvaluationFunctions {

    private static final Logger LOGGER = LogManager.getLogger(DoidEvaluationFunctions.class);

    private DoidEvaluationFunctions() {
    }

    @NotNull
    public static EvaluationResult hasExclusiveTumorTypeOfDoid(@NotNull DoidModel doidModel, @NotNull Set<String> patientDoids,
            @NotNull String doidToMatch, @NotNull Set<String> failDoids, @NotNull Set<String> warnDoids) {
        boolean allDoidsAreMatch = true;
        boolean hasAtLeastOneFailDoid = false;
        boolean hasAtLeastOneWarnDoid = false;
        for (String doid : patientDoids) {
            Set<String> doidTree = doidModel.doidWithParents(doid);
            if (!doidTree.contains(doidToMatch)) {
                allDoidsAreMatch = false;
            }

            for (String failDoid : failDoids) {
                if (doidTree.contains(failDoid)) {
                    hasAtLeastOneFailDoid = true;
                    break;
                }
            }

            for (String warnSolidCancer : warnDoids) {
                if (doidTree.contains(warnSolidCancer)) {
                    hasAtLeastOneWarnDoid = true;
                    break;
                }
            }
        }

        if (allDoidsAreMatch && !hasAtLeastOneFailDoid) {
            return hasAtLeastOneWarnDoid ? EvaluationResult.WARN : EvaluationResult.PASS;
        }

        return EvaluationResult.FAIL;
    }

    public static boolean hasTumorOfCertainType(@NotNull DoidModel doidModel, @NotNull TumorDetails tumor, @NotNull Set<String> validDoids,
            @NotNull Set<String> validDoidTerms, @NotNull Set<String> validPrimaryTumorExtraDetails) {
        Set<String> doids = tumor.doids();
        if (doids != null) {
            for (String doid : doids) {
                Set<String> expandedDoids = doidModel.doidWithParents(doid);
                for (String validDoid : validDoids) {
                    if (expandedDoids.contains(validDoid)) {
                        return true;
                    }
                }

                for (String expandedDoid : expandedDoids) {
                    String term = doidModel.resolveTermForDoid(expandedDoid);
                    if (term == null) {
                        LOGGER.warn("Could not resolve term for doid '{}'", expandedDoid);
                    } else {
                        for (String validDoidTerm : validDoidTerms) {
                            if (term.toLowerCase().contains(validDoidTerm.toLowerCase())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        String primaryTumorExtraDetails = tumor.primaryTumorExtraDetails();
        if (primaryTumorExtraDetails != null) {
            String lowerCaseDetails = primaryTumorExtraDetails.toLowerCase();
            for (String validPrimaryTumorExtraDetail : validPrimaryTumorExtraDetails) {
                if (lowerCaseDetails.contains(validPrimaryTumorExtraDetail.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }
}
