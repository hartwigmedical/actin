package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class SurgeryRuleMapper extends RuleMapper {

    public SurgeryRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HAD_RECENT_SURGERY, hasHadRecentSurgeryCreator());
        map.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, hasHadSurgeryInPastWeeksCreator());
        map.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_MONTHS, hasHadSurgeryInPastMonthsCreator());
        return map;
    }

    @NotNull
    private FunctionCreator hasHadRecentSurgeryCreator() {
        LocalDate evaluationDate = referenceDateProvider().date();
        LocalDate minDate = evaluationDate.minusMonths(2);
        return function -> new HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate);
    }

    @NotNull
    private FunctionCreator hasHadSurgeryInPastWeeksCreator() {
        return function -> {
            LocalDate evaluationDate = referenceDateProvider().date();

            int maxAgeWeeks = functionInputResolver().createOneIntegerInput(function);
            LocalDate minDate = evaluationDate.minusWeeks(maxAgeWeeks).plusWeeks(2);

            return new HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate);
        };
    }

    @NotNull
    private FunctionCreator hasHadSurgeryInPastMonthsCreator() {
        return function -> {
            LocalDate evaluationDate = referenceDateProvider().date();

            int maxAgeMonths = functionInputResolver().createOneIntegerInput(function);
            LocalDate minDate = evaluationDate.minusMonths(maxAgeMonths);

            return new HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate);
        };
    }
}
