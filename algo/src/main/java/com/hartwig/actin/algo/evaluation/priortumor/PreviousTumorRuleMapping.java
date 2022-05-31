package com.hartwig.actin.algo.evaluation.priortumor;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class PreviousTumorRuleMapping {

    private PreviousTumorRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ACTIVE_SECOND_MALIGNANCY, hasActiveSecondMalignancyCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, hasHistoryOfSecondMalignancyCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X,
                hasHistoryOfSecondMalignancyWithDoidCreator(doidModel));
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS,
                hasHistoryOfSecondMalignancyWithinYearsCreator(referenceDateProvider));

        return map;
    }

    @NotNull
    private static FunctionCreator hasActiveSecondMalignancyCreator() {
        return function -> new HasActiveSecondMalignancy();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyCreator() {
        return function -> new HasHistoryOfSecondMalignancy();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyWithDoidCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doidToMatch = FunctionInputResolver.createOneStringInput(function);
            return new HasHistoryOfSecondMalignancyWithDOID(doidModel, doidToMatch);
        };
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyWithinYearsCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            int maxYears = FunctionInputResolver.createOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider.date().minusYears(maxYears);

            return new HasHistoryOfSecondMalignancyWithinYears(minDate);
        };
    }
}
