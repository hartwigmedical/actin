package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class SurgeryRuleMapping {

    private SurgeryRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HAD_RECENT_SURGERY, function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, hasHadRecentSurgeryCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasHadRecentSurgeryCreator() {
        return function -> {
            int maxAgeWeeks = FunctionInputResolver.createOneIntegerInput(function);
            LocalDate minDate = EvaluationConstants.REFERENCE_DATE.minusWeeks(maxAgeWeeks);
            return new HasHadRecentSurgery(minDate);
        };
    }
}
