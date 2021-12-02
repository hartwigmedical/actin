package com.hartwig.actin.algo.evaluation.general;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.jetbrains.annotations.NotNull;

public final class GeneralRuleMapping {

    private GeneralRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, isAtLeast18YearsOldCreator());
        map.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, hasMaximumWHOStatusCreator());
        map.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_GIVE_ADEQUATE_INFORMED_CONSENT, canGiveAdequateInformedConsentCreator());
        map.put(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES, isInvolvedInStudyProceduresCreator());
        map.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, hasSufficientLifeExpectancyCreator());
        map.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS, hasSufficientLifeExpectancyCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator isAtLeast18YearsOldCreator() {
        return function -> new IsAtLeastEighteenYearsOld(EvaluationConstants.REFERENCE_YEAR);
    }

    @NotNull
    private static FunctionCreator hasMaximumWHOStatusCreator() {
        return function -> {
            int maximumWHO = EligibilityParameterResolver.createOneIntegerParameter(function);
            return new HasMaximumWHOStatus(maximumWHO);
        };
    }

    @NotNull
    private static FunctionCreator canGiveAdequateInformedConsentCreator() {
        return function -> new CanGiveAdequateInformedConsent();
    }

    @NotNull
    private static FunctionCreator isInvolvedInStudyProceduresCreator() {
        return function -> new IsInvolvedInStudyProcedures();
    }

    @NotNull
    private static FunctionCreator hasSufficientLifeExpectancyCreator() {
        return function -> new HasSufficientLifeExpectancy();
    }
}
