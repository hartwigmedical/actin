package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DoidEvaluationFunctions {

    private static final Logger LOGGER = LogManager.getLogger(DoidEvaluationFunctions.class);

    private DoidEvaluationFunctions() {
    }

    public static boolean hasConfiguredDoids(@Nullable Set<String> tumorDoids) {
        return tumorDoids != null && !tumorDoids.isEmpty();
    }

    public static boolean isOfSpecificDoid(@NotNull DoidModel doidModel, @NotNull Set<String> tumorDoids, @NotNull String doidToMatch) {
        return isOfAtLeastOneSpecificDoid(doidModel, tumorDoids, Sets.newHashSet(doidToMatch));
    }

    public static boolean isOfAtLeastOneSpecificDoid(@NotNull DoidModel doidModel, @NotNull Set<String> tumorDoids,
            @NotNull Set<String> doidsToMatch) {
        Set<String> fullExpandedDoidTree = createFullExpandedDoidTree(doidModel, tumorDoids);
        for (String doidToMatch : doidsToMatch) {
            if (fullExpandedDoidTree.contains(doidToMatch)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isOfExactDoid(@NotNull Set<String> tumorDoids, @NotNull String doidToMatch) {
        return tumorDoids.equals(Sets.newHashSet(doidToMatch));
    }

    public static boolean isOfSpecificDoidCombination(@NotNull Set<String> tumorDoids, @NotNull Set<String> validDoidCombination) {
        Set<Set<String>> validDoidCombinations = Sets.newHashSet();
        validDoidCombinations.add(validDoidCombination);

        return hasAtLeastOneCombinationOfDoids(tumorDoids, validDoidCombinations);
    }

    public static boolean isOfExclusiveDoid(@NotNull DoidModel doidModel, @NotNull Set<String> tumorDoids, @NotNull String doidToMatch) {
        EvaluationResult result =
                evaluateForExclusiveMatchWithFailAndWarns(doidModel, tumorDoids, doidToMatch, Sets.newHashSet(), Sets.newHashSet());

        return result == EvaluationResult.PASS;
    }

    public static boolean isOfSpecificDoidOrTerm(@NotNull DoidModel doidModel, @NotNull Set<String> tumorDoids,
            @NotNull Set<String> validDoids, @NotNull Set<String> validDoidTerms) {
        for (String tumorDoid : tumorDoids) {
            Set<String> expandedTumorDoids = doidModel.expandedDoidWithParents(tumorDoid);
            for (String validDoid : validDoids) {
                if (expandedTumorDoids.contains(validDoid)) {
                    return true;
                }
            }

            for (String doid : expandedTumorDoids) {
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

        return false;
    }

    @NotNull
    public static EvaluationResult evaluateForExclusiveMatchWithFailAndWarns(@NotNull DoidModel doidModel, @NotNull Set<String> tumorDoids,
            @NotNull String doidToMatch, @NotNull Set<String> failDoids, @NotNull Set<String> warnDoids) {
        boolean allDoidsAreMatch = true;
        boolean hasAtLeastOneFailDoid = false;
        boolean hasAtLeastOneWarnDoid = false;
        for (String doid : tumorDoids) {
            Set<String> expandedDoids = doidModel.expandedDoidWithParents(doid);
            if (!expandedDoids.contains(doidToMatch)) {
                allDoidsAreMatch = false;
            }

            for (String failDoid : failDoids) {
                if (expandedDoids.contains(failDoid)) {
                    hasAtLeastOneFailDoid = true;
                    break;
                }
            }

            for (String warnDoid : warnDoids) {
                if (expandedDoids.contains(warnDoid)) {
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

    @VisibleForTesting
    static boolean hasAtLeastOneCombinationOfDoids(@NotNull Set<String> tumorDoids, @NotNull Set<Set<String>> validDoidCombinations) {
        for (Set<String> validDoidCombination : validDoidCombinations) {
            boolean containsAll = true;
            for (String doid : validDoidCombination) {
                if (!tumorDoids.contains(doid)) {
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
