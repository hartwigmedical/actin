package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionFunctions {

    private OtherConditionFunctions() {
    }

    @NotNull
    public static List<PriorOtherCondition> selectClinicallyRelevant(@NotNull List<PriorOtherCondition> priorOtherConditions) {
        List<PriorOtherCondition> filtered = Lists.newArrayList();
        for (PriorOtherCondition priorOtherCondition : priorOtherConditions) {
            if (priorOtherCondition.isContraindicationForTherapy()) {
                filtered.add(priorOtherCondition);
            }
        }
        return filtered;
    }
}
