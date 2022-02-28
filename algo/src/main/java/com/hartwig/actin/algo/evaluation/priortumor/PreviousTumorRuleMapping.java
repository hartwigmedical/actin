package com.hartwig.actin.algo.evaluation.priortumor;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class PreviousTumorRuleMapping {

    private PreviousTumorRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, hasHistoryOfSecondMalignancyCreator(doidModel, false));
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X,
                hasHistoryOfSecondMalignancyWithDoidCreator(doidModel, false));
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X_CURRENTLY_INACTIVE,
                hasHistoryOfSecondMalignancyWithDoidCreator(doidModel, true));
        map.put(EligibilityRule.EVERY_SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS, secondMalignanciesHaveBeenCuredRecentlyCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyWithDoidCreator(@NotNull DoidModel doidModel, boolean mustBeInactive) {
        return function -> {
            String doidToMatch = FunctionInputResolver.createOneStringInput(function);
            return new HasHistoryOfSecondMalignancy(doidModel, doidToMatch, mustBeInactive);
        };
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyCreator(@NotNull DoidModel doidModel, boolean mustBeInactive) {
        return function -> new HasHistoryOfSecondMalignancy(doidModel, null, mustBeInactive);
    }

    @NotNull
    private static FunctionCreator secondMalignanciesHaveBeenCuredRecentlyCreator() {
        return function -> new SecondMalignanciesHaveBeenCuredRecently();
    }
}
