package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.single.OneTreatmentCategoryOneString;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TreatmentRuleMapping {

    private TreatmentRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, hasExhaustedSOCTreatmentsCreator());
        map.put(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS, hasDeclinedSOCTreatmentsCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, hasHistoryOfSecondMalignancyCreator(doidModel, null, false));
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X,
                hasHistoryOfSecondMalignancyWithDoidCreator(doidModel, false));
        map.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X_CURRENTLY_INACTIVE,
                hasHistoryOfSecondMalignancyWithDoidCreator(doidModel, true));
        map.put(EligibilityRule.EVERY_SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS, secondMalignanciesHaveBeenCuredRecentlyCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, hasHadLimitedSystemicTreatmentsCreator());
        map.put(EligibilityRule.HAS_HAD_DRUG_NAME_X_TREATMENT, hasHadTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT, hasHadTreatmentCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y, hasHadTreatmentCategoryOfTypeCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPE_Y, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AND_AT_MOST_Z_LINES, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_HAD_FLUOROPYRIMIDINE_TREATMENT, hasHadFluoropyrimidineTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_TAXANE_TREATMENT, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES, hasHadLimitedAntiPDL1OrPD1ImmunotherapiesCreator());
        map.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X, isEligibleForOnLabelDrugCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasExhaustedSOCTreatmentsCreator() {
        return function -> new HasExhaustedSOCTreatments();
    }

    @NotNull
    private static FunctionCreator hasDeclinedSOCTreatmentsCreator() {
        return function -> new HasDeclinedSOCTreatments();
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyWithDoidCreator(@NotNull DoidModel doidModel, boolean mustBeInactive) {
        return function -> {
            String doidToMatch = FunctionInputResolver.createOneStringInput(function);
            return new HasHistoryOfSecondMalignancy(doidModel, doidToMatch, mustBeInactive);
        };
    }

    @NotNull
    private static FunctionCreator hasHistoryOfSecondMalignancyCreator(@NotNull DoidModel doidModel, @Nullable String doidToMatch,
            boolean mustBeInactive) {
        return function -> new HasHistoryOfSecondMalignancy(doidModel, doidToMatch, mustBeInactive);
    }

    @NotNull
    private static FunctionCreator secondMalignanciesHaveBeenCuredRecentlyCreator() {
        return function -> new SecondMalignanciesHaveBeenCuredRecently();
    }

    @NotNull
    private static FunctionCreator hasHadLimitedSystemicTreatmentsCreator() {
        return function -> {
            int maxSystemicTreatments = FunctionInputResolver.createOneIntegerInput(function);
            return new HasHadLimitedSystemicTreatments(maxSystemicTreatments);
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCreator() {
        return function -> {
            String treatment = FunctionInputResolver.createOneStringInput(function);
            return new HasHadTreatment(treatment);
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryCreator() {
        return function -> {
            TreatmentCategory category = FunctionInputResolver.createOneTreatmentCategoryInput(function);
            return new HasHadTreatmentCategory(category, null);
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryOfTypeCreator() {
        return function -> {
            OneTreatmentCategoryOneString input = FunctionInputResolver.createOneTreatmentCategoryOneStringInput(function);
            return new HasHadTreatmentCategory(input.treatmentCategory(), input.string());
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedAntiPDL1OrPD1ImmunotherapiesCreator() {
        return function -> {
            int maxAntiPDL1OrPD1Immunotherapies = FunctionInputResolver.createOneIntegerInput(function);
            return new HasHadLimitedAntiPDL1OrPD1Immunotherapies(maxAntiPDL1OrPD1Immunotherapies);
        };
    }

    @NotNull
    private static FunctionCreator hasHadFluoropyrimidineTreatmentCreator() {
        return function -> new HasHadFluoropyrimidineTreatment();
    }


    @NotNull
    private static FunctionCreator isEligibleForOnLabelDrugCreator() {
        return function -> new IsEligibleForOnLabelDrug();
    }
}
