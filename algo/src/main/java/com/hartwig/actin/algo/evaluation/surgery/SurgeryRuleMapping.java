package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.EvaluationConstants;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class SurgeryRuleMapping {

    private SurgeryRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HAD_RECENT_SURGERY, hasHadSurgeryCreator());
        map.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, hasHadSurgeryInPastWeeksCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasHadSurgeryCreator() {
        return function -> new HasHadSurgery();
    }

    @NotNull
    private static FunctionCreator hasHadSurgeryInPastWeeksCreator() {
        return function -> {
            int maxAgeWeeks = FunctionInputResolver.createOneIntegerInput(function);
            LocalDate minDate = EvaluationConstants.REFERENCE_DATE.minusWeeks(maxAgeWeeks).plusWeeks(2);

            return new HasHadRecentSurgery(minDate);
        };
    }
}
