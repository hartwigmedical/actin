package com.hartwig.actin.algo.evaluation.general;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class GeneralRuleMapping {

    private GeneralRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, hasAtLeastCertainAgeCreator());
        map.put(EligibilityRule.IS_MALE, isMaleCreator());
        map.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, hasMaximumWHOStatusCreator());
        map.put(EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT, canGiveAdequateInformedConsentCreator());
        map.put(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES, isInvolvedInStudyProceduresCreator());
        map.put(EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL, participatesInAnotherTrialCreator());
        map.put(EligibilityRule.HAS_PARTICIPATED_IN_CURRENT_TRIAL, hasParticipatedInCurrentTrialCreator());
        map.put(EligibilityRule.HAS_RAPIDLY_DETORIATING_CONDITION, hasRapidlyDetoriatingConditionCreator());
        map.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, hasSufficientLifeExpectancyCreator());
        map.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS, hasSufficientLifeExpectancyCreator());
        map.put(EligibilityRule.PATIENT_IS_TREATED_IN_HOSPITAL_X, patientIsTreatedInHospitalCreator());
        map.put(EligibilityRule.PATIENT_WILL_BE_PARTICIPATING_IN_COUNTRY_X, patientWillBeParticipatingInCountryCreator());
        map.put(EligibilityRule.PATIENT_IS_LEGALLY_INSTITUTIONALIZED, isLegallyInstitutionalizedCreator());
        map.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_NOT_USE_CONTACT_LENSES, isWillingToNotUseContactLensesCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasAtLeastCertainAgeCreator() {
        return function -> {
            int minAge = FunctionInputResolver.createOneIntegerInput(function);
            return new HasAtLeastCertainAge(EvaluationConstants.REFERENCE_YEAR, minAge);
        };
    }

    @NotNull
    private static FunctionCreator isMaleCreator() {
        return function -> new IsMale();
    }

    @NotNull
    private static FunctionCreator hasMaximumWHOStatusCreator() {
        return function -> {
            int maximumWHO = FunctionInputResolver.createOneIntegerInput(function);
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
    private static FunctionCreator participatesInAnotherTrialCreator() {
        return function -> new ParticipatesInAnotherTrial();
    }

    @NotNull
    private static FunctionCreator hasParticipatedInCurrentTrialCreator() {
        return function -> new HasParticipatedInCurrentTrial();
    }

    @NotNull
    private static FunctionCreator hasRapidlyDetoriatingConditionCreator() {
        return function -> new HasRapidlyDetoriatingCondition();
    }

    @NotNull
    private static FunctionCreator hasSufficientLifeExpectancyCreator() {
        return function -> new HasSufficientLifeExpectancy();
    }

    @NotNull
    private static FunctionCreator patientIsTreatedInHospitalCreator() {
        return function -> new PatientIsTreatedInHospital();
    }

    @NotNull
    private static FunctionCreator patientWillBeParticipatingInCountryCreator() {
        return function -> {
            String country = FunctionInputResolver.createOneStringInput(function);
            return new PatientWillBeParticipatingInCountry(country);
        };
    }

    @NotNull
    private static FunctionCreator isLegallyInstitutionalizedCreator() {
        return function -> new IsLegallyInstitutionalized();
    }

    @NotNull
    private static FunctionCreator isWillingToNotUseContactLensesCreator() {
        return function -> new IsWillingToNotUseContactLenses();
    }
}
