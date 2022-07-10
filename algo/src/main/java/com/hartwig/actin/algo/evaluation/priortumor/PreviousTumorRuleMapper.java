package com.hartwig.actin.algo.evaluation.priortumor;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class PreviousTumorRuleMapper extends RuleMapper {

    public PreviousTumorRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ACTIVE_SECOND_MALIGNANCY, hasActiveSecondMalignancyCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, hasHistoryOfSecondMalignancyCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X, hasHistoryOfSecondMalignancyWithDoidCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_TERM_X,
                hasHistoryOfSecondMalignancyWithDoidTermCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS, hasHistoryOfSecondMalignancyWithinYearsCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasActiveSecondMalignancyCreator() {
        return function -> new HasActiveSecondMalignancy();
    }

    @NotNull
    private FunctionCreator hasHistoryOfSecondMalignancyCreator() {
        return function -> new HasHistoryOfSecondMalignancy();
    }

    @NotNull
    private FunctionCreator hasHistoryOfSecondMalignancyWithDoidCreator() {
        return function -> {
            String doidToMatch = functionInputResolver().createOneStringInput(function);
            return new HasHistoryOfSecondMalignancyWithDoid(doidModel(), doidToMatch);
        };
    }

    @NotNull
    private FunctionCreator hasHistoryOfSecondMalignancyWithDoidTermCreator() {
        return function -> {
            // TODO Map DOID term to DOID
            String doidTermToMatch = functionInputResolver().createOneDoidTermInput(function);
            return new HasHistoryOfSecondMalignancyWithDoid(doidModel(), doidTermToMatch);
        };
    }

    @NotNull
    private FunctionCreator hasHistoryOfSecondMalignancyWithinYearsCreator() {
        return function -> {
            int maxYears = functionInputResolver().createOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider().date().minusYears(maxYears);

            return new HasHistoryOfSecondMalignancyWithinYears(minDate);
        };
    }
}
