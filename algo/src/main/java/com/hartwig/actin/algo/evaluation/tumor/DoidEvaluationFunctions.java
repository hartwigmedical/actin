package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
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

    public static boolean isOfDoidType(@NotNull DoidModel doidModel, @NotNull Set<String> patientDoids, @NotNull String doidToMatch) {
        Set<String> fullExpandedDoidTree = createFullExpandedDoidTree(doidModel, patientDoids);
        return fullExpandedDoidTree.contains(doidToMatch);
    }

    @NotNull
    public static EvaluationResult hasExclusiveDoidOfType(@NotNull DoidModel doidModel, @NotNull Set<String> patientDoids,
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

            for (String warnDoid : warnDoids) {
                if (doidTree.contains(warnDoid)) {
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

    public static boolean hasDoidOfCertainType(@NotNull DoidModel doidModel, @NotNull TumorDetails tumor, @NotNull Set<String> validDoids,
            @NotNull Set<String> validDoidTerms) {
        Set<String> patientDoids = tumor.doids();
        if (patientDoids != null) {
            for (String patientDoid : patientDoids) {
                Set<String> doidTree = doidModel.doidWithParents(patientDoid);
                for (String validDoid : validDoids) {
                    if (doidTree.contains(validDoid)) {
                        return true;
                    }
                }

                for (String doid : doidTree) {
                    String term = doidModel.resolveTermForDoid(doid);
                    if (term == null) {
                        LOGGER.warn("Could not resolve term for doid '{}'", doid);
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

        return false;
    }

    public static boolean hasSpecificCombinationOfDoids(@NotNull Set<String> patientDoids,
            @NotNull Set<Set<String>> validDoidCombinations) {
        for (Set<String> validDoidCombination : validDoidCombinations) {
            boolean containsAll = true;
            for (String doid : validDoidCombination) {
                if (!patientDoids.contains(doid)) {
                    containsAll = false;
                    break;
                }
            }

            if (containsAll) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static Set<String> createFullExpandedDoidTree(@NotNull DoidModel doidModel, @NotNull Set<String> doidsToExpand) {
        Set<String> expanded = Sets.newHashSet();
        for (String doid : doidsToExpand) {
            expanded.addAll(doidModel.expandedDoidWithParents(doid));
        }
        return expanded;
    }
}
