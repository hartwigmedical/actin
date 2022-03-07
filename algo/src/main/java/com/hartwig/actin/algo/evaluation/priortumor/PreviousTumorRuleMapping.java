package com.hartwig.actin.algo.evaluation.priortumor;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.EvaluationConstants;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class PreviousTumorRuleMapping {

    private PreviousTumorRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ACTIVE_SECOND_MALIGNANCY, hasActiveSecondMalignancyCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X,
                hasHistoryOfSecondMalignancyWithDoidCreator(doidModel));
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS, hasHistoryOfSecondMalignancyWithinYearsCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasActiveSecondMalignancyCreator() {
        return function -> new HasActiveSecondMalignancy();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyWithDoidCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doidToMatch = FunctionInputResolver.createOneStringInput(function);
            return new HasHistoryOfSecondMalignancyWithDOID(doidModel, doidToMatch);
        };
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyWithinYearsCreator() {
        return function -> {
            int maxYears = FunctionInputResolver.createOneIntegerInput(function);
            return new HasHistoryOfSecondMalignancyWithinYears(EvaluationConstants.REFERENCE_DATE, maxYears);
        };
    }
}
