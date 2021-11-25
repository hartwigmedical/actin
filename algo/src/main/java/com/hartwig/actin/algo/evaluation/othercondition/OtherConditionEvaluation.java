package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.List;

import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

final class OtherConditionEvaluation {

    private OtherConditionEvaluation() {
    }

    public static boolean hasDoid(@NotNull DoidModel doidModel, @NotNull List<PriorOtherCondition> priorOtherConditions,
            @NotNull String doidToFind) {
        for (PriorOtherCondition priorOtherCondition : priorOtherConditions) {
            for (String doid : priorOtherCondition.doids()) {
                if (doidModel.doidWithParents(doid).contains(doidToFind)) {
                    return true;
                }
            }
        }

        return false;
    }
}
