package com.hartwig.actin.algo.evaluation.treatment;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.OneTreatmentOneInteger;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStrings;
import com.hartwig.actin.treatment.input.single.OneTypedTreatmentManyStringsOneInteger;

import org.jetbrains.annotations.NotNull;

public class TreatmentRuleMapper extends RuleMapper {

    public TreatmentRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT, isEligibleForCurativeTreatmentCreator());
        map.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, hasExhaustedSOCTreatmentsCreator());
        map.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X, isEligibleForOnLabelTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES, hasHadSomeApprovedTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES, hasHadSomeSystemicTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, hasHadLimitedSystemicTreatmentsCreator());
        map.put(EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT, hasHadAnyCancerTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORIES_WITH_NAME_X,
                hasHadAnyCancerTreatmentIgnoringCategoriesAndNamesCreator());
        map.put(EligibilityRule.HAS_HAD_TREATMENT_NAME_X, hasHadSpecificTreatmentCreator());
        map.put(EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS, hasHadSpecificTreatmentWithinWeeksCreator());
        map.put(EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS, hasHadCombinedTreatmentNamesWithinWeeksCreator());
        map.put(EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES,
                hasHadCombinedTreatmentNamesWithCyclesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT, hasHadTreatmentWithCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y, hasHadTreatmentCategoryOfTypesCreator());
        map.put(EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y, hasHadFirstLineTreatmentCategoryOfTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS, hasHadTreatmentCategoryOfTypesWithinWeeksCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y, hasHadTreatmentCategoryIgnoringTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES, hasHadSomeTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES, hasHadLimitedTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES,
                hasHadSomeTreatmentsOfCategoryWithTypesCreator());
        map.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES,
                hasHadLimitedTreatmentsOfCategoryWithTypesCreator());
        map.put(EligibilityRule.HAS_RECEIVED_HER2_TARGETING_ADC, hasReceivedHER2TargetingADCCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT, hasProgressiveDiseaseFollowingTreatmentNameCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT,
                hasProgressiveDiseaseFollowingTreatmentCategoryCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y,
                hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS,
                hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES,
                hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES,
                hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator());
        map.put(EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES,
                hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator());
        map.put(EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE,
                hasRadiologicalProgressionFollowingLatestTreatmentLineCreator());
        map.put(EligibilityRule.HAS_HAD_COMPLETE_RESECTION, hasHadCompleteResectionCreator());
        map.put(EligibilityRule.HAS_HAD_PARTIAL_RESECTION, hasHadPartialResectionCreator());
        map.put(EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS, hasHadResectionWithinWeeksCreator());
        map.put(EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS, hasHadLocalHepaticTherapyWithinWeeksCreator());
        map.put(EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT, hasHadIntratumoralInjectionTreatmentCreator());
        map.put(EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT,
                hasLimitedCumulativeAnthracyclineExposureCreator());
        map.put(EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL, hasPreviouslyParticipatedInCurrentTrialCreator());
        map.put(EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL, participatesInAnotherTrialCreator());

        return map;
    }

    @NotNull
    private FunctionCreator isEligibleForCurativeTreatmentCreator() {
        return function -> new IsEligibleForCurativeTreatment();
    }

    @NotNull
    private FunctionCreator hasExhaustedSOCTreatmentsCreator() {
        return function -> new HasExhaustedSOCTreatments();
    }

    @NotNull
    private FunctionCreator isEligibleForOnLabelTreatmentCreator() {
        return function -> new IsEligibleForOnLabelTreatment();
    }

    @NotNull
    private FunctionCreator hasHadSomeApprovedTreatmentCreator() {
        return function -> {
            int minApprovedTreatments = functionInputResolver().createOneIntegerInput(function);
            return new HasHadSomeApprovedTreatments(minApprovedTreatments);
        };
    }

    @NotNull
    private FunctionCreator hasHadSomeSystemicTreatmentCreator() {
        return function -> {
            int minSystemicTreatments = functionInputResolver().createOneIntegerInput(function);
            return new HasHadSomeSystemicTreatments(minSystemicTreatments);
        };
    }

    @NotNull
    private FunctionCreator hasHadLimitedSystemicTreatmentsCreator() {
        return function -> {
            int maxSystemicTreatments = functionInputResolver().createOneIntegerInput(function);
            return new HasHadLimitedSystemicTreatments(maxSystemicTreatments);
        };
    }

    @NotNull
    private FunctionCreator hasHadAnyCancerTreatmentCreator() {
        return function -> new HasHadAnyCancerTreatment();
    }

    @NotNull
    private FunctionCreator hasHadAnyCancerTreatmentIgnoringCategoriesAndNamesCreator() {
        return function -> new HasHadAnyCancerTreatmentIgnoringCategoriesAndNames();
    }

    @NotNull
    private FunctionCreator hasHadSpecificTreatmentCreator() {
        return function -> {
            String treatment = functionInputResolver().createOneStringInput(function);
            return new HasHadSomeSpecificTreatments(Sets.newHashSet(treatment), null, 1);
        };
    }

    @NotNull
    private FunctionCreator hasHadSpecificTreatmentWithinWeeksCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider().date().minusWeeks(input.integer());
            return new HasHadSpecificTreatmentSinceDate(input.string(), minDate);
        };
    }

    @NotNull
    private FunctionCreator hasHadCombinedTreatmentNamesWithinWeeksCreator() {
        return function -> new HasHadCombinedTreatmentNamesWithinWeeks();
    }

    @NotNull
    private FunctionCreator hasHadCombinedTreatmentNamesWithCyclesCreator() {
        return function -> new HasHadCombinedTreatmentNamesWithCycles();
    }

    @NotNull
    private FunctionCreator hasHadTreatmentWithCategoryCreator() {
        return function -> {
            TreatmentInput treatment = functionInputResolver().createOneTreatmentInput(function);
            if (treatment.mappedNames() == null) {
                return new HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), 1);
            } else {
                return new HasHadSomeSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), 1);
            }
        };
    }

    @NotNull
    private FunctionCreator hasHadTreatmentCategoryOfTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStrings input = functionInputResolver().createOneTypedTreatmentManyStringsInput(function);
            return new HasHadSomeTreatmentsWithCategoryOfTypes(input.category(), input.strings(), 1);
        };
    }

    @NotNull
    private FunctionCreator hasHadFirstLineTreatmentCategoryOfTypesCreator() {
        return function -> new HasHadFirstLineTreatmentCategoryOfTypes();
    }

    @NotNull
    private FunctionCreator hasHadTreatmentCategoryOfTypesWithinWeeksCreator() {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function);

            LocalDate minDate = referenceDateProvider().date().minusWeeks(input.integer());
            return new HasHadTreatmentWithCategoryOfTypesRecently(input.category(), input.strings(), minDate);
        };
    }

    @NotNull
    private FunctionCreator hasHadTreatmentCategoryIgnoringTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStrings input = functionInputResolver().createOneTypedTreatmentManyStringsInput(function);
            return new HasHadTreatmentWithCategoryButNotOfTypes(input.category(), input.strings());
        };
    }

    @NotNull
    private FunctionCreator hasHadSomeTreatmentsOfCategoryCreator() {
        return function -> {
            OneTreatmentOneInteger input = functionInputResolver().createOneTreatmentOneIntegerInput(function);
            TreatmentInput treatment = input.treatment();
            if (treatment.mappedNames() == null) {
                return new HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), input.integer());
            } else {
                return new HasHadSomeSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), input.integer());
            }
        };
    }

    @NotNull
    private FunctionCreator hasHadLimitedTreatmentsOfCategoryCreator() {
        return function -> {
            OneTreatmentOneInteger input = functionInputResolver().createOneTreatmentOneIntegerInput(function);
            TreatmentInput treatment = input.treatment();
            if (treatment.mappedNames() == null) {
                return new HasHadLimitedTreatmentsWithCategory(treatment.mappedCategory(), input.integer());
            } else {
                return new HasHadLimitedSpecificTreatments(treatment.mappedNames(), treatment.mappedCategory(), input.integer());
            }
        };
    }

    @NotNull
    private FunctionCreator hasHadSomeTreatmentsOfCategoryWithTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function);
            return new HasHadSomeTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator hasHadLimitedTreatmentsOfCategoryWithTypesCreator() {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function);
            return new HasHadLimitedTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator hasReceivedHER2TargetingADCCreator() {
        return function -> new HasReceivedHER2TargetingADC();
    }

    @NotNull
    private FunctionCreator hasProgressiveDiseaseFollowingTreatmentNameCreator() {
        return function -> {
            String nameToFind = functionInputResolver().createOneStringInput(function);
            return new HasHadPDFollowingSpecificTreatment(Sets.newHashSet(nameToFind), null);
        };
    }

    @NotNull
    private FunctionCreator hasProgressiveDiseaseFollowingTreatmentCategoryCreator() {
        return function -> {
            TreatmentInput treatment = functionInputResolver().createOneTreatmentInput(function);
            if (treatment.mappedNames() == null) {
                return new HasHadPDFollowingTreatmentWithCategory(treatment.mappedCategory());
            } else {
                return new HasHadPDFollowingSpecificTreatment(treatment.mappedNames(), treatment.mappedCategory());
            }
        };
    }

    @NotNull
    private FunctionCreator hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator() {
        return function -> {
            OneTypedTreatmentManyStrings input = functionInputResolver().createOneTypedTreatmentManyStringsInput(function);
            return new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category(), input.strings(), null, null);
        };
    }

    @NotNull
    private FunctionCreator hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator() {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function);
            return new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category(),
                    input.strings(),
                    input.integer(),
                    null);
        };
    }

    //TODO: Check implementation
    @NotNull
    private FunctionCreator hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator() {
        return function -> {
            OneTypedTreatmentManyStringsOneInteger input =
                    functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(function);
            return new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category(),
                    input.strings(),
                    null,
                    input.integer());
        };
    }

    @NotNull
    private FunctionCreator hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator() {
        return function -> {
            int minSystemicTreatments = functionInputResolver().createOneIntegerInput(function);
            return new HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, false);
        };
    }

    @NotNull
    private FunctionCreator hasRadiologicalProgressionFollowingLatestTreatmentLineCreator() {
        return function -> new HasRadiologicalProgressionFollowingLatestTreatmentLine();
    }

    //TODO: Check implementation
    @NotNull
    private FunctionCreator hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator() {
        return function -> {
            int minSystemicTreatments = functionInputResolver().createOneIntegerInput(function);
            return new HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, true);
        };
    }

    @NotNull
    private FunctionCreator hasHadCompleteResectionCreator() {
        return function -> new HasHadCompleteResection();
    }

    @NotNull
    private FunctionCreator hasHadPartialResectionCreator() {
        return function -> new HasHadPartialResection();
    }

    @NotNull
    private FunctionCreator hasHadResectionWithinWeeksCreator() {
        return function -> {
            int maxWeeksAgo = functionInputResolver().createOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider().date().minusWeeks(maxWeeksAgo);
            return new HasHadRecentResection(minDate);
        };
    }

    @NotNull
    private FunctionCreator hasHadLocalHepaticTherapyWithinWeeksCreator() {
        return function -> new HasHadLocalHepaticTherapyWithinWeeks();
    }

    @NotNull
    private FunctionCreator hasHadIntratumoralInjectionTreatmentCreator() {
        return function -> new HasHadIntratumoralInjectionTreatment();
    }

    @NotNull
    private FunctionCreator hasLimitedCumulativeAnthracyclineExposureCreator() {
        return function -> new HasLimitedCumulativeAnthracyclineExposure(doidModel());
    }

    @NotNull
    private FunctionCreator hasPreviouslyParticipatedInCurrentTrialCreator() {
        return function -> new HasPreviouslyParticipatedInCurrentTrial();
    }

    @NotNull
    private FunctionCreator participatesInAnotherTrialCreator() {
        return function -> new ParticipatesInAnotherTrial();
    }
}
