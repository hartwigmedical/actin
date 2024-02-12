package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

class TreatmentRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.IS_NOT_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT to isNotEligibleForCurativeTreatmentCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X to isEligibleForOnLabelTreatmentCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_PALLIATIVE_RADIOTHERAPY to isEligibleForPalliativeRadiotherapyCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_LOCO_REGIONAL_THERAPY to isEligibleForLocoRegionalTherapyCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X to isEligibleForTreatmentLinesCreator(),
            EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS to hasExhaustedSOCTreatmentsCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES to hasHadSomeApprovedTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES to hasHadSomeSystemicTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES to hasHadLimitedSystemicTreatmentsCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT to hasHadAnyCancerTreatmentCreator(),
            EligibilityRule.HAS_NOT_RECEIVED_ANY_CANCER_TREATMENT_WITHIN_X_MONTHS to hasHadAnyCancerTreatmentWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X to hasHadSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS to hasHadSpecificTreatmentWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X to hasHadTreatmentWithDrugsCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS to hasHadCombinedTreatmentNamesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES to hasHadCombinedTreatmentNamesWithCyclesCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_WITHIN_Y_WEEKS to hasHadTreatmentWithAnyDrugSinceDateCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT to hasHadTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentCategoryOfAllTypesCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadFirstLineTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS to hasHadTreatmentCategoryOfTypesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y to hasHadTreatmentCategoryIgnoringTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_DRUGS_Y to hasHadTreatmentCategoryIgnoringDrugsCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES to hasHadSomeTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES to hasHadLimitedTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentsOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES to hasHadLimitedTreatmentsOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AS_MOST_RECENT_LINE to hasHadTreatmentCategoryOfTypesAsMostRecentCreator(),
            EligibilityRule.HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT to hasHadAdjuvantTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X_WITHIN_Y_MONTHS to hasHadSystemicTherapyWithIntentsWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X to hasHadSystemicTherapyWithIntentsCreator(),
            EligibilityRule.HAS_HAD_NON_INTERNAL_RADIOTHERAPY to FunctionCreator { HasHadNonInternalRadiotherapy() },
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentNameCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_TREATMENT_WITH_ANY_DRUG_X to hasProgressiveDiseaseFollowingTreatmentWithAnyDrugCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE to hasRadiologicalProgressionFollowingLatestTreatmentLineCreator(),
            EligibilityRule.HAS_HAD_COMPLETE_RESECTION to hasHadCompleteResectionCreator(),
            EligibilityRule.HAS_HAD_PARTIAL_RESECTION to hasHadPartialResectionCreator(),
            EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS to hasHadResectionWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS to hasHadLocalHepaticTherapyWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT to hasHadIntratumoralInjectionTreatmentCreator(),
            EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT to hasLimitedCumulativeAnthracyclineExposureCreator(),
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL to hasPreviouslyParticipatedInCurrentTrialCreator(),
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL to hasPreviouslyParticipatedInTrialCreator(),
            EligibilityRule.IS_NOT_PARTICIPATING_IN_ANOTHER_TRIAL to isNotParticipatingInAnotherTrialCreator()
        )
    }

    private fun isNotEligibleForCurativeTreatmentCreator(): FunctionCreator {
        return FunctionCreator { IsNotEligibleForCurativeTreatment() }
    }

    private fun isEligibleForOnLabelTreatmentCreator(): FunctionCreator {
        return FunctionCreator { IsEligibleForOnLabelTreatment() }
    }

    private fun isEligibleForPalliativeRadiotherapyCreator(): FunctionCreator {
        return FunctionCreator { IsEligibleForPalliativeRadiotherapy() }
    }

    private fun isEligibleForLocoRegionalTherapyCreator(): FunctionCreator {
        return FunctionCreator { IsEligibleForLocoRegionalTherapy() }
    }

    private fun isEligibleForTreatmentLinesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val lines = functionInputResolver().createManyIntegersInput(function)
            IsEligibleForTreatmentLines(lines)
        }
    }

    private fun hasExhaustedSOCTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { HasExhaustedSOCTreatments(RecommendationEngineFactory(resources)) }
    }

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

    private fun hasHadAnyCancerTreatmentWithinMonthsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val monthsAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusMonths(monthsAgo.toLong())
            HasNotReceivedAnyCancerTreatmentSinceDate(minDate, monthsAgo)
        }
    }

    private fun hasHadSpecificTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneSpecificTreatmentInput(function)
            HasHadSomeSpecificTreatments(listOf(treatment), 1)
        }
    }

    private fun hasHadSpecificTreatmentWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneSpecificTreatmentOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadSpecificTreatmentSinceDate(input.treatment, minDate)
        }
    }

    private fun hasHadTreatmentWithDrugsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            HasHadTreatmentWithDrug(functionInputResolver().createManyDrugsInput(function))
        }
    }

    private fun hasHadCombinedTreatmentNamesWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { HasHadCombinedTreatmentNamesWithinWeeks() }
    }

    private fun hasHadCombinedTreatmentNamesWithCyclesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManySpecificTreatmentsTwoIntegerInput(function)
            HasHadCombinedTreatmentNamesWithCycles(input.treatments, input.integer1, input.integer2)
        }
    }

    private fun hasHadTreatmentWithAnyDrugSinceDateCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyDrugsOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadTreatmentWithAnyDrugSinceDate(input.drugs, minDate)
        }
    }

    private fun hasHadTreatmentWithCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            if (treatment.mappedType == null) {
                HasHadSomeTreatmentsWithCategory(treatment.mappedCategory, 1)
            } else {
                HasHadSomeTreatmentsWithCategoryOfTypes(treatment.mappedCategory, setOf(treatment.mappedType!!), 1)
            }
        }
    }

    private fun hasHadTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category, input.types, 1)
        }
    }

    private fun hasHadSomeTreatmentCategoryOfAllTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadSomeTreatmentsWithCategoryOfAllTypes(input.category, input.types, input.integer)
        }
    }

    private fun hasHadFirstLineTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return FunctionCreator { HasHadFirstLineTreatmentCategoryOfTypes() }
    }

    private fun hasHadTreatmentCategoryOfTypesWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadTreatmentWithCategoryOfTypesRecently(input.category, input.types, minDate)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadTreatmentWithCategoryButNotOfTypes(input.category, input.types)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringDrugsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyDrugsInput(function)
            HasHadTreatmentWithCategoryButNotWithDrugs(input.category, input.drugs)
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryOrTypeOneIntegerInput(function)
            val treatment = input.treatment
            if (treatment.mappedType == null) {
                HasHadSomeTreatmentsWithCategory(treatment.mappedCategory, input.integer)
            } else {
                HasHadSomeTreatmentsWithCategoryOfTypes(treatment.mappedCategory, setOf(treatment.mappedType!!), input.integer)
            }
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryOrTypeOneIntegerInput(function)
            val treatment = input.treatment
            if (treatment.mappedType == null) {
                return@FunctionCreator HasHadLimitedTreatmentsWithCategory(treatment.mappedCategory, input.integer)
            } else {
                return@FunctionCreator HasHadLimitedTreatmentsWithCategoryOfTypes(
                    treatment.mappedCategory,
                    setOf(treatment.mappedType!!),
                    input.integer
                )
            }
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(
                function
            )
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category, input.types, input.integer)
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(
                function
            )
            HasHadLimitedTreatmentsWithCategoryOfTypes(input.category, input.types, input.integer)
        }
    }

    private fun hasHadAdjuvantTreatmentWithCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            if (treatment.mappedType == null) {
                return@FunctionCreator HasHadAdjuvantTreatmentWithCategory(treatment.mappedCategory)
            } else {
                return@FunctionCreator HasHadAdjuvantTreatmentWithCategoryOfTypes(
                    setOf(treatment.mappedType!!),
                    treatment.mappedCategory
                )
            }
        }
    }

    private fun hasHadSystemicTherapyWithIntentsWithinMonthsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyIntentsOneIntegerInput(function)
            val monthsAgo = input.integer
            val minDate = referenceDateProvider().date().minusMonths(monthsAgo.toLong())
            HasHadSystemicTherapyWithAnyIntent(input.intents, minDate, monthsAgo)
        }
    }

    private fun hasHadSystemicTherapyWithIntentsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyIntentsInput(function)
            HasHadSystemicTherapyWithAnyIntent(input.intents, null, null)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesAsMostRecentCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(input.mappedCategory, input.mappedType)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentNameCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneSpecificTreatmentInput(function)
            HasHadPDFollowingSpecificTreatment(listOf(treatment))
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            val mappedType = treatment.mappedType
            if (mappedType == null) {
                HasHadPDFollowingTreatmentWithCategory(treatment.mappedCategory)
            } else {
                HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(treatment.mappedCategory, setOf(mappedType), null, null)
            }
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(
                function
            )
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category, input.types, null, null)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(
                function
            )
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
                input.category,
                input.types,
                input.integer,
                null
            )
        }
    }

    //TODO: Check implementation
    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(
                function
            )
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
                input.category,
                input.types,
                null,
                input.integer
            )
        }
    }

    private fun hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, false)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentWithAnyDrugCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val drugs = functionInputResolver().createManyDrugsInput(function)
            HasHadPDFollowingTreatmentWithAnyDrug(drugs)
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

    private fun hasPreviouslyParticipatedInTrialCreator(): FunctionCreator {
        return FunctionCreator { HasPreviouslyParticipatedInTrial() }
    }

    private fun isNotParticipatingInAnotherTrialCreator(): FunctionCreator {
        return FunctionCreator { IsNotParticipatingInAnotherTrial() }
    }
}