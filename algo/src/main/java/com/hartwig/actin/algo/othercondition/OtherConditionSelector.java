package com.hartwig.actin.algo.othercondition;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionSelector {

    private OtherConditionSelector() {
    }

    @NotNull
    public static List<PriorOtherCondition> selectClinicallyRelevant(@NotNull List<PriorOtherCondition> conditions) {
        List<PriorOtherCondition> filtered = Lists.newArrayList();
        for (PriorOtherCondition condition : conditions) {
            if (condition.isContraindicationForTherapy()) {
                filtered.add(condition);
            }
        }
        return filtered;
    }
}
