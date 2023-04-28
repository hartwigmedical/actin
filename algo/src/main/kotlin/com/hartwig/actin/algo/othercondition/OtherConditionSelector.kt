package com.hartwig.actin.algo.othercondition;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionSelector {

    private OtherConditionSelector() {
    }

    @NotNull
    public static Set<PriorOtherCondition> selectClinicallyRelevant(@NotNull List<PriorOtherCondition> conditions) {
        return conditions.stream().filter(PriorOtherCondition::isContraindicationForTherapy).collect(Collectors.toSet());
    }

    public static Set<String> selectConditionsMatchingDoid(@NotNull List<PriorOtherCondition> conditions, @NotNull String doidToFind,
            @NotNull DoidModel doidModel) {
        return selectClinicallyRelevant(conditions).stream()
                .filter(condition -> conditionHasDoid(condition, doidToFind, doidModel))
                .map(PriorOtherCondition::name)
                .collect(Collectors.toSet());
    }

    private static boolean conditionHasDoid(@NotNull PriorOtherCondition condition, @NotNull String doidToFind,
            @NotNull DoidModel doidModel) {
        return condition.doids().stream().anyMatch(doid -> doidModel.doidWithParents(doid).contains(doidToFind));
    }
}