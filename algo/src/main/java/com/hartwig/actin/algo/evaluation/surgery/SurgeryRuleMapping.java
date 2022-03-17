package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class SurgeryRuleMapping {

    private SurgeryRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HAD_RECENT_SURGERY, hasHadAnySurgeryCreator());
        map.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, hasHadSurgeryInPastWeeksCreator(referenceDateProvider));

        return map;
    }

    @NotNull
    private static FunctionCreator hasHadAnySurgeryCreator() {
        return function -> new HasHadAnySurgery();
    }

    @NotNull
    private static FunctionCreator hasHadSurgeryInPastWeeksCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            int maxAgeWeeks = FunctionInputResolver.createOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider.date().minusWeeks(maxAgeWeeks).plusWeeks(2);

            return new HasHadSurgeryInPastWeeks(minDate);
        };
    }
}
