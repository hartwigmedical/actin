package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

final class DoidEvaluationFunctions {

    private DoidEvaluationFunctions() {
    }

    @NotNull
    public static EvaluationResult evaluate(@NotNull DoidModel doidModel, @NotNull Set<String> patientDoids, @NotNull String doidToMatch,
            @NotNull Set<String> failDoids, @NotNull Set<String> warnDoids) {
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
}
