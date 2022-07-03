package com.hartwig.actin.algo.evaluation.treatment;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStringsOneInteger;

import org.jetbrains.annotations.NotNull;

public final class TreatmentRuleMapping {

    private TreatmentRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT, isEligibleForCurativeTreatmentCreator());
        map.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, hasExhaustedSOCTreatmentsCreator());
        map.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X, isEligibleForOnLabelDrugCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES, hasHadSomeApprovedTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES, hasHadSomeSystemicTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, hasHadLimitedSystemicTreatmentsCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES, hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator());
        map.put(EligibilityRule.HAS_HAD_TREATMENT_NAME_X, hasHadSpecificTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT, hasHadTreatmentWithCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y, hasHadTreatmentCategoryOfTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS,
                hasHadTreatmentCategoryOfTypesWithinWeeksCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y, hasHadTreatmentCategoryIgnoringTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES, hasHadSomeTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES, hasHadLimitedTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES,
                hasHadSomeTreatmentsOfCategoryWithTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES,
                hasHadLimitedTreatmentsOfCategoryWithTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITH_STOP_REASON_PD,
                hasHadSomeTreatmentsOfCategoryWithStopReasonPDCreator());
        map.put(EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT, hadHadIntratumoralInjectionTreatmentCreator());
        map.put(EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL, participatesInAnotherTrialCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator isEligibleForCurativeTreatmentCreator() {
        return function -> new IsEligibleForCurativeTreatment();
    }

    @NotNull
    private static FunctionCreator hasExhaustedSOCTreatmentsCreator() {
        return function -> new HasExhaustedSOCTreatments();
    }

    @NotNull
    private static FunctionCreator isEligibleForOnLabelDrugCreator() {
        return function -> new IsEligibleForOnLabelDrug();
    }

    @NotNull
    private static FunctionCreator hasHadSomeApprovedTreatmentCreator() {
        return function -> {
            int minApprovedTreatments = FunctionInputResolver.createOneIntegerInput(function);
            return new HasHadSomeApprovedTreatments(minApprovedTreatments);
        };
    }

    @NotNull
    private static FunctionCreator hasHadSomeSystemicTreatmentCreator() {
        return function -> {
            int minSystemicTreatments = FunctionInputResolver.createOneIntegerInput(function);
            return new HasHadSomeSystemicTreatments(minSystemicTreatments);
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedSystemicTreatmentsCreator() {
        return function -> {
            int maxSystemicTreatments = FunctionInputResolver.createOneIntegerInput(function);
            return new HasHadLimitedSystemicTreatments(maxSystemicTreatments);
        };
    }

    @NotNull
    private static FunctionCreator hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator() {
        return function -> {
            int minSystemicTreatments = FunctionInputResolver.createOneIntegerInput(function);
            return new HasProgressiveDiseaseFollowingSomeSystemicTreatments(minSystemicTreatments);
        };
    }

    @NotNull
    private static FunctionCreator hasHadSpecificTreatmentCreator() {
        return function -> {
            String treatment = FunctionInputResolver.createOneStringInput(function);
            return new HasHadSomeSpecificTreatments(Sets.newHashSet(treatment), null, 1);
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentWithCategoryCreator() {
        return function -> {
            TreatmentInput treatment = FunctionInputResolver.createOneTreatmentInput(function);
            if (treatment.mappedNames() == null) {
                return new HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), 1);
            } else {
                return new HasHadSomeSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), 1);
            }
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryOfTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStrings input = FunctionInputResolver.createOneTypedTreatmentManyStringsInput(function);
            return new HasHadTreatmentWithCategoryOfTypes(input.category(), input.strings());
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryOfTypesWithinWeeksCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    FunctionInputResolver.createOneTypedTreatmentManyStringsOneIntegerInput(function);

            LocalDate minDate = referenceDateProvider.date().minusWeeks(input.integer());
            return new HasHadTreatmentWithCategoryOfTypesRecently(input.category(), input.strings(), minDate);
        };
    }

    @NotNull
    private static FunctionCreator hasHadTreatmentCategoryIgnoringTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStrings input = FunctionInputResolver.createOneTypedTreatmentManyStringsInput(function);
            return new HasHadTreatmentWithCategoryButNotOfTypes(input.category(), input.strings());
        };
    }

    @NotNull
    private static FunctionCreator hasHadSomeTreatmentsOfCategoryCreator() {
        return function -> {
            OneTreatmentOneInteger input = FunctionInputResolver.createOneTreatmentOneIntegerInput(function);
            TreatmentInput treatment = input.treatment();
            if (treatment.mappedNames() == null) {
                return new HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), input.integer());
            } else {
                return new HasHadSomeSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), input.integer());
            }
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedTreatmentsOfCategoryCreator() {
        return function -> {
            OneTreatmentOneInteger input = FunctionInputResolver.createOneTreatmentOneIntegerInput(function);
            TreatmentInput treatment = input.treatment();
            if (treatment.mappedNames() == null) {
                return new HasHadLimitedTreatmentsWithCategory(treatment.mappedCategory(), input.integer());
            } else {
                return new HasHadLimitedSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), input.integer());
            }
        };
    }

    @NotNull
    private static FunctionCreator hasHadSomeTreatmentsOfCategoryWithTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    FunctionInputResolver.createOneTypedTreatmentManyStringsOneIntegerInput(function);
            return new HasHadSomeTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer());
        };
    }

    @NotNull
    private static FunctionCreator hasHadLimitedTreatmentsOfCategoryWithTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    FunctionInputResolver.createOneTypedTreatmentManyStringsOneIntegerInput(function);
            return new HasHadLimitedTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer());
        };
    }

    @NotNull
    private static FunctionCreator hasHadSomeTreatmentsOfCategoryWithStopReasonPDCreator() {
        return function -> {
            OneTypedTreatmentManyStrings input = FunctionInputResolver.createOneTypedTreatmentManyStringsInput(function);
            return new HasHadTreatmentWithCategoryOfTypesAndStopReasonPD(input.category(), input.strings());
        };
    }

    @NotNull
    private static FunctionCreator hadHadIntratumoralInjectionTreatmentCreator() {
        return function -> new HadHadIntratumoralInjectionTreatment();
    }

    @NotNull
    private static FunctionCreator participatesInAnotherTrialCreator() {
        return function -> new ParticipatesInAnotherTrial();
    }
}
