package com.hartwig.actin.algo.evaluation.treatment

import com.google.common.collect.Sets
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class TreatmentRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT to isEligibleForCurativeTreatmentCreator,
            EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS to hasExhaustedSOCTreatmentsCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X to isEligibleForOnLabelTreatmentCreator,
            EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES to hasHadSomeApprovedTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES to hasHadSomeSystemicTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES to hasHadLimitedSystemicTreatmentsCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT to hasHadAnyCancerTreatmentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X to hasHadSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS to hasHadSpecificTreatmentWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS to hasHadCombinedTreatmentNamesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES to hasHadCombinedTreatmentNamesWithCyclesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT to hasHadTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadFirstLineTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS to hasHadTreatmentCategoryOfTypesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y to hasHadTreatmentCategoryIgnoringTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES to hasHadSomeTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES to hasHadLimitedTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentsOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES to hasHadLimitedTreatmentsOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_RECEIVED_HER2_TARGETING_ADC to hasReceivedHER2TargetingADCCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentNameCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE to hasRadiologicalProgressionFollowingLatestTreatmentLineCreator(),
            EligibilityRule.HAS_HAD_COMPLETE_RESECTION to hasHadCompleteResectionCreator(),
            EligibilityRule.HAS_HAD_PARTIAL_RESECTION to hasHadPartialResectionCreator(),
            EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS to hasHadResectionWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS to hasHadLocalHepaticTherapyWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT to hasHadIntratumoralInjectionTreatmentCreator(),
            EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT to hasLimitedCumulativeAnthracyclineExposureCreator(),
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL to hasPreviouslyParticipatedInCurrentTrialCreator(),
            EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL to participatesInAnotherTrialCreator()
        )
    }

    private val isEligibleForCurativeTreatmentCreator: FunctionCreator
        get() = FunctionCreator { IsEligibleForCurativeTreatment() }

    private fun hasExhaustedSOCTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { HasExhaustedSOCTreatments() }
    }

    private val isEligibleForOnLabelTreatmentCreator: FunctionCreator
        get() = FunctionCreator { IsEligibleForOnLabelTreatment() }

    private fun hasHadSomeApprovedTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minApprovedTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadSomeApprovedTreatments(minApprovedTreatments)
        }
    }

    private fun hasHadSomeSystemicTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadSomeSystemicTreatments(minSystemicTreatments)
        }
    }

    private fun hasHadLimitedSystemicTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadLimitedSystemicTreatments(maxSystemicTreatments)
        }
    }

    private fun hasHadAnyCancerTreatmentCreator(): FunctionCreator {
        return FunctionCreator { HasHadAnyCancerTreatment() }
    }

    private fun hasHadSpecificTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneStringInput(function)
            HasHadSomeSpecificTreatments(Sets.newHashSet(treatment), null, 1)
        }
    }

    private fun hasHadSpecificTreatmentWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer().toLong())
            HasHadSpecificTreatmentSinceDate(input.string(), minDate)
        }
    }

    private fun hasHadCombinedTreatmentNamesWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { HasHadCombinedTreatmentNamesWithinWeeks() }
    }

    private fun hasHadCombinedTreatmentNamesWithCyclesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsTwoIntegersInput(function)
            HasHadCombinedTreatmentNamesWithCycles(input.strings(), input.integer1(), input.integer2())
        }
    }

    private fun hasHadTreatmentWithCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentInput(function)
            if (treatment.mappedNames() == null) {
                return@FunctionCreator HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), 1)
            } else {
                return@FunctionCreator HasHadSomeSpecificTreatments(treatment.mappedNames()!!, treatment.mappedCategory(), 1)
            }
        }
    }

    private fun hasHadTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsInput(
                function
            )
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category(), input.strings(), 1)
        }
    }

    private fun hasHadFirstLineTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return FunctionCreator { HasHadFirstLineTreatmentCategoryOfTypes() }
    }

    private fun hasHadTreatmentCategoryOfTypesWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(
                function
            )
            val minDate = referenceDateProvider().date().minusWeeks(input.integer().toLong())
            HasHadTreatmentWithCategoryOfTypesRecently(input.category(), input.strings(), minDate)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsInput(
                function
            )
            HasHadTreatmentWithCategoryButNotOfTypes(input.category(), input.strings())
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentOneIntegerInput(function)
            val treatment = input.treatment()
            if (treatment.mappedNames() == null) {
                return@FunctionCreator HasHadSomeTreatmentsWithCategory(treatment.mappedCategory(), input.integer())
            } else {
                return@FunctionCreator HasHadSomeSpecificTreatments(treatment.mappedNames()!!, treatment.mappedCategory(), input.integer())
            }
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentOneIntegerInput(function)
            val treatment = input.treatment()
            if (treatment.mappedNames() == null) {
                return@FunctionCreator HasHadLimitedTreatmentsWithCategory(treatment.mappedCategory(), input.integer())
            } else {
                return@FunctionCreator HasHadLimitedSpecificTreatments(
                    treatment.mappedNames()!!,
                    treatment.mappedCategory(),
                    input.integer()
                )
            }
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(
                function
            )
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer())
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(
                function
            )
            HasHadLimitedTreatmentsWithCategoryOfTypes(input.category(), input.strings(), input.integer())
        }
    }

    private fun hasReceivedHER2TargetingADCCreator(): FunctionCreator {
        return FunctionCreator { HasReceivedHER2TargetingADC() }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentNameCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val nameToFind = functionInputResolver().createOneStringInput(function)
            HasHadPDFollowingSpecificTreatment(Sets.newHashSet(nameToFind), null)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentInput(function)
            val mappedNames = treatment.mappedNames()
            if (mappedNames == null) {
                HasHadPDFollowingTreatmentWithCategory(treatment.mappedCategory())
            } else {
                HasHadPDFollowingSpecificTreatment(mappedNames, treatment.mappedCategory())
            }
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsInput(
                function
            )
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category(), input.strings(), null, null)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(
                function
            )
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
                input.category(),
                input.strings(),
                input.integer(),
                null
            )
        }
    }

    //TODO: Check implementation
    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTypedTreatmentManyStringsOneIntegerInput(
                function
            )
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
                input.category(),
                input.strings(),
                null,
                input.integer()
            )
        }
    }

    private fun hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, false)
        }
    }

    private fun hasRadiologicalProgressionFollowingLatestTreatmentLineCreator(): FunctionCreator {
        return FunctionCreator { HasRadiologicalProgressionFollowingLatestTreatmentLine() }
    }

    //TODO: Check implementation
    private fun hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, true)
        }
    }

    private fun hasHadCompleteResectionCreator(): FunctionCreator {
        return FunctionCreator { HasHadCompleteResection() }
    }

    private fun hasHadPartialResectionCreator(): FunctionCreator {
        return FunctionCreator { HasHadPartialResection() }
    }

    private fun hasHadResectionWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxWeeksAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(maxWeeksAgo.toLong())
            HasHadRecentResection(minDate)
        }
    }

    private fun hasHadLocalHepaticTherapyWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { HasHadLocalHepaticTherapyWithinWeeks() }
    }

    private fun hasHadIntratumoralInjectionTreatmentCreator(): FunctionCreator {
        return FunctionCreator { HasHadIntratumoralInjectionTreatment() }
    }

    private fun hasLimitedCumulativeAnthracyclineExposureCreator(): FunctionCreator {
        return FunctionCreator { HasLimitedCumulativeAnthracyclineExposure(doidModel()) }
    }

    private fun hasPreviouslyParticipatedInCurrentTrialCreator(): FunctionCreator {
        return FunctionCreator { HasPreviouslyParticipatedInCurrentTrial() }
    }

    private fun participatesInAnotherTrialCreator(): FunctionCreator {
        return FunctionCreator { ParticipatesInAnotherTrial() }
    }
}