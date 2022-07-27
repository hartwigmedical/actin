package com.hartwig.actin.algo.evaluation.general;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class GeneralRuleMapper extends RuleMapper {

    public GeneralRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, hasAtLeastCertainAgeCreator());
        map.put(EligibilityRule.IS_MALE, isMaleCreator());
        map.put(EligibilityRule.IS_FEMALE, isFemaleCreator());
        map.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, hasMaximumWHOStatusCreator());
        map.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_EXACTLY_X, hasWHOStatusCreator());
        map.put(EligibilityRule.HAS_KARNOFSKY_SCORE_OF_AT_LEAST_X, hasMinimumKarnofskyScoreCreator());
        map.put(EligibilityRule.HAS_LANSKY_SCORE_OF_AT_LEAST_X, hasMinimumLanskyScoreCreator());
        map.put(EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT, canGiveAdequateInformedConsentCreator());
        map.put(EligibilityRule.HAS_RAPIDLY_DETERIORATING_CONDITION, hasRapidlyDeterioratingConditionCreator());
        map.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, hasSufficientLifeExpectancyCreator());
        map.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS, hasSufficientLifeExpectancyCreator());
        map.put(EligibilityRule.IS_TREATED_IN_HOSPITAL_X, isTreatedInHospitalCreator());
        map.put(EligibilityRule.WILL_PARTICIPATE_IN_TRIAL_IN_COUNTRY_X, willParticipateInTrialInCountryCreator());
        map.put(EligibilityRule.IS_LEGALLY_INSTITUTIONALIZED, isLegallyInstitutionalizedCreator());
        map.put(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES, isInvolvedInStudyProceduresCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasAtLeastCertainAgeCreator() {
        return function -> {
            int minAge = functionInputResolver().createOneIntegerInput(function);
            return new HasAtLeastCertainAge(referenceDateProvider().year(), minAge);
        };
    }

    @NotNull
    private FunctionCreator isMaleCreator() {
        return function -> new IsMale();
    }

    @NotNull
    private FunctionCreator isFemaleCreator() {
        return function -> new IsFemale();
    }

    @NotNull
    private FunctionCreator hasMaximumWHOStatusCreator() {
        return function -> {
            int maximumWHO = functionInputResolver().createOneIntegerInput(function);
            return new HasMaximumWHOStatus(maximumWHO);
        };
    }

    @NotNull
    private FunctionCreator hasWHOStatusCreator() {
        return function -> {
            int requiredWHO = functionInputResolver().createOneIntegerInput(function);
            return new HasWHOStatus(requiredWHO);
        };
    }

    @NotNull
    private FunctionCreator hasMinimumKarnofskyScoreCreator() {
        return function -> new HasMinimumKarnofskyScore();
    }

    @NotNull
    private FunctionCreator hasMinimumLanskyScoreCreator() {
        return function -> new HasMinimumLanskyScore();
    }

    @NotNull
    private FunctionCreator canGiveAdequateInformedConsentCreator() {
        return function -> new CanGiveAdequateInformedConsent();
    }

    @NotNull
    private FunctionCreator isInvolvedInStudyProceduresCreator() {
        return function -> new IsInvolvedInStudyProcedures();
    }

    @NotNull
    private FunctionCreator hasRapidlyDeterioratingConditionCreator() {
        return function -> new HasRapidlyDeterioratingCondition();
    }

    @NotNull
    private FunctionCreator hasSufficientLifeExpectancyCreator() {
        return function -> new HasSufficientLifeExpectancy();
    }

    @NotNull
    private FunctionCreator isTreatedInHospitalCreator() {
        return function -> new IsTreatedInHospital();
    }

    @NotNull
    private FunctionCreator willParticipateInTrialInCountryCreator() {
        return function -> {
            String country = functionInputResolver().createOneStringInput(function);
            return new WillParticipateInTrialInCountry(country);
        };
    }

    @NotNull
    private FunctionCreator isLegallyInstitutionalizedCreator() {
        return function -> new IsLegallyInstitutionalized();
    }
}
