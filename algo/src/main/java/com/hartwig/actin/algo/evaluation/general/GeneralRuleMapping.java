package com.hartwig.actin.algo.evaluation.general;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.jetbrains.annotations.NotNull;

public final class GeneralRuleMapping {

    private GeneralRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> functionCreatorMap = Maps.newHashMap();

        functionCreatorMap.put(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, isAtLeast18YearsOldCreator());
        functionCreatorMap.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, hasMaximumWHOStatusCreator());
        functionCreatorMap.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_GIVE_ADEQUATE_INFORMED_CONSENT,
                canGiveAdequateInformedConsentCreator());
        functionCreatorMap.put(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES, isInvolvedInStudyProceduresCreator());
        functionCreatorMap.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, hasSufficientLifeExpectancyCreator());
        functionCreatorMap.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS, hasSufficientLifeExpectancyCreator());

        return functionCreatorMap;
    }

    @NotNull
    private static FunctionCreator isAtLeast18YearsOldCreator() {
        return function -> new IsAtLeastEighteenYearsOld(LocalDate.now().getYear());
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
