package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.tumor.HasAcquiredResistanceToAnyDrug
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
            EligibilityRule.IS_ELIGIBLE_FOR_LOCAL_LIVER_TREATMENT to isEligibleForLocalLiverTreatmentCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_INTENSIVE_TREATMENT to isEligibleForIntensiveTreatmentCreator(),
            EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS to hasExhaustedSOCTreatmentsCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES to hasHadSomeApprovedTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES to hasHadSomeSystemicTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES to hasHadLimitedSystemicTreatmentsCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT to hasHadAnyCancerTreatmentCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORY_X to hasHadAnyCancerTreatmentIgnoringSomeCategoryCreator(),
            EligibilityRule.HAS_NOT_RECEIVED_ANY_CANCER_TREATMENT_WITHIN_X_MONTHS to hasHadAnyCancerTreatmentWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X to hasHadSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS to hasHadSpecificTreatmentWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_TREATMENT_NAME_X to hasHadFirstLineTreatmentNameCreator(),
            EligibilityRule.HAS_HAD_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z to hasHadSpecificDrugCombinedWithCategoryAndTypesCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X to hasHadTreatmentWithDrugsCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_AS_MOST_RECENT_LINE to hasHadTreatmentWithAnyDrugAsMostRecentCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS to hasHadCombinedTreatmentNamesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES to hasHadCombinedTreatmentNamesWithCyclesCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_WITHIN_Y_WEEKS to hasHadTreatmentWithAnyDrugSinceDateCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT to hasHadTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_RECEIVED_PLATINUM_BASED_DOUBLET to hasReceivedPlatinumBasedDoubletCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentCategoryOfAllTypesCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadFirstLineTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS to hasHadTreatmentCategoryOfTypesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y to hasHadTreatmentCategoryIgnoringTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y_WITHIN_Z_WEEKS to hasHadTreatmentCategoryIgnoringTypesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_DRUGS_Y to hasHadTreatmentCategoryIgnoringDrugsCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_IGNORING_DRUGS_Z to hasHadTreatmentCategoryOfTypesIgnoringDrugsCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES to hasHadSomeTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES to hasHadLimitedTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentsOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES to hasHadLimitedTreatmentsOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITH_STOP_REASON_OTHER_THAN_PD to hasHadTreatmentsOfCategoryWithTypesAndStopReasonNotPDCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_FOR_AT_MOST_Z_WEEKS_WITH_STOP_REASON_OTHER_THAN_PD
                    to hasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPDCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AS_MOST_RECENT_LINE to hasHadTreatmentCategoryOfTypesAsMostRecentCreator(),
            EligibilityRule.HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT to hasHadAdjuvantTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITHIN_X_WEEKS to hasHadSystemicTherapyWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X_WITHIN_Y_WEEKS to hasHadSystemicTherapyWithIntentsWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X to hasHadSystemicTherapyWithIntentsCreator(),
            EligibilityRule.HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_NAME_X_TREATMENT to hasHadClinicalBenefitFollowingSomeTreatmentCreator(),
            EligibilityRule.HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_CATEGORY_X_TREATMENT to hasHadClinicalBenefitFollowingTreatmentOfCategoryCreator(),
            EligibilityRule.HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadClinicalBenefitFollowingTreatmentOfCategoryAndTypesCreator(),
            EligibilityRule.HAS_HAD_SOC_TARGETED_THERAPY_FOR_NSCLC to hasHadSocTargetedTherapyForNsclcCreator(),
            EligibilityRule.HAS_HAD_SOC_TARGETED_THERAPY_FOR_NSCLC_EXCLUDING_DRIVER_GENES_X to hasHadSocTargetedTherapyForNsclcExcludingSomeGenesCreator(),
            EligibilityRule.HAS_HAD_TARGETED_THERAPY_INTERFERING_WITH_RAS_MEK_MAPK_PATHWAY to hasHadTargetedTherapyInterferingWithRasMekMapkPathwayCreator(),
            EligibilityRule.HAS_HAD_NON_INTERNAL_RADIOTHERAPY to { HasHadNonInternalRadiotherapy() },
            EligibilityRule.HAS_HAD_RADIOTHERAPY_TO_BODY_LOCATION_X to hasHadRadiotherapyToSomeBodyLocationCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentNameCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_TREATMENT_WITH_ANY_DRUG_X to hasProgressiveDiseaseFollowingTreatmentWithAnyDrugCreator(),
            EligibilityRule.HAS_ACQUIRED_RESISTANCE_TO_ANY_DRUG_X to hasAcquiredResistanceToSomeDrugCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE to hasRadiologicalProgressionFollowingLatestTreatmentLineCreator(),
            EligibilityRule.HAS_HAD_COMPLETE_RESECTION to hasHadCompleteResectionCreator(),
            EligibilityRule.HAS_HAD_PARTIAL_RESECTION to hasHadPartialResectionCreator(),
            EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS to hasHadResectionWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_LIVER_RESECTION to hasHadLiverResectionCreator(),
            EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS to hasHadLocalHepaticTherapyWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT to hasHadIntratumoralInjectionTreatmentCreator(),
            EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT to hasLimitedCumulativeAnthracyclineExposureCreator(),
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_CURRENT_TRIAL to hasPreviouslyParticipatedInCurrentTrialCreator(),
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL to hasPreviouslyParticipatedInTrialCreator(),
            EligibilityRule.IS_NOT_PARTICIPATING_IN_ANOTHER_TRIAL to isNotParticipatingInAnotherTrialCreator(),
            EligibilityRule.HAS_RECEIVED_SYSTEMIC_TREATMENT_FOR_BRAIN_METASTASES to hasReceivedSystemicTherapyforBrainMetastasesCreator(),
            EligibilityRule.HAS_HAD_BRAIN_RADIATION_THERAPY to hasHadBrainRadiationTherapyCreator(),
        )
    }

    private fun isNotEligibleForCurativeTreatmentCreator(): FunctionCreator {
        return { IsNotEligibleForCurativeTreatment() }
    }

    private fun isEligibleForOnLabelTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatmentName = functionInputResolver().createOneSpecificTreatmentInput(function)
            IsEligibleForOnLabelTreatment(treatmentName, RecommendationEngineFactory(resources))
        }
    }

    private fun isEligibleForPalliativeRadiotherapyCreator(): FunctionCreator {
        return { IsEligibleForPalliativeRadiotherapy() }
    }

    private fun isEligibleForLocoRegionalTherapyCreator(): FunctionCreator {
        return { IsEligibleForLocoRegionalTherapy() }
    }

    private fun isEligibleForTreatmentLinesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val lines = functionInputResolver().createManyIntegersInput(function)
            IsEligibleForTreatmentLines(lines)
        }
    }

    private fun isEligibleForLocalLiverTreatmentCreator(): FunctionCreator {
        return { IsEligibleForLocalLiverTreatment(doidModel()) }
    }

    private fun isEligibleForIntensiveTreatmentCreator(): FunctionCreator {
        return { IsEligibleForIntensiveTreatment() }
    }

    private fun hasExhaustedSOCTreatmentsCreator(): FunctionCreator {
        return { HasExhaustedSOCTreatments(RecommendationEngineFactory(resources), doidModel()) }
    }

    private fun hasHadSomeApprovedTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minApprovedTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadSomeApprovedTreatments(minApprovedTreatments)
        }
    }

    private fun hasHadSomeSystemicTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadSomeSystemicTreatments(minSystemicTreatments)
        }
    }

    private fun hasHadLimitedSystemicTreatmentsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadLimitedSystemicTreatments(maxSystemicTreatments)
        }
    }

    private fun hasHadAnyCancerTreatmentCreator(): FunctionCreator {
        return { HasHadAnyCancerTreatment(null) }
    }

    private fun hasHadAnyCancerTreatmentIgnoringSomeCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            HasHadAnyCancerTreatment(treatment.mappedCategory)
        }
    }

    private fun hasHadAnyCancerTreatmentWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val monthsAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusMonths(monthsAgo.toLong())
            HasNotReceivedAnyCancerTreatmentSinceDate(minDate, monthsAgo)
        }
    }

    private fun hasHadSpecificTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneSpecificTreatmentInput(function)
            HasHadSomeSpecificTreatments(listOf(treatment), 1)
        }
    }

    private fun hasHadSpecificTreatmentWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneSpecificTreatmentOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadSpecificTreatmentSinceDate(input.treatment, minDate)
        }
    }

    private fun hasHadFirstLineTreatmentNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneSpecificTreatmentInput(function)
            HasHadFirstLineTreatmentName(input.name)
        }
    }

    private fun hasHadSpecificDrugCombinedWithCategoryAndTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneSpecificDrugOneTreatmentCategoryManyTypesInput(function)
            HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(input.drug, input.category, input.types)
        }
    }

    private fun hasHadTreatmentWithDrugsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasHadTreatmentWithDrug(functionInputResolver().createManyDrugsInput(function))
        }
    }

    private fun hasHadTreatmentWithAnyDrugAsMostRecentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasHadTreatmentWithDrugFromSetAsMostRecent(functionInputResolver().createManyDrugsInput(function))
        }
    }

    private fun hasHadCombinedTreatmentNamesWithinWeeksCreator(): FunctionCreator {
        return { HasHadCombinedTreatmentNamesWithinWeeks() }
    }

    private fun hasHadCombinedTreatmentNamesWithCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManySpecificTreatmentsTwoIntegerInput(function)
            HasHadCombinedTreatmentNamesWithCycles(input.treatments, input.integer1, input.integer2)
        }
    }

    private fun hasHadTreatmentWithAnyDrugSinceDateCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyDrugsOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadTreatmentWithAnyDrugSinceDate(input.drugs, minDate)
        }
    }

    private fun hasHadTreatmentWithCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            if (treatment.mappedType == null) {
                HasHadSomeTreatmentsWithCategory(treatment.mappedCategory, 1)
            } else {
                HasHadSomeTreatmentsWithCategoryOfTypes(treatment.mappedCategory, setOf(treatment.mappedType!!), 1)
            }
        }
    }

    private fun hasHadTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category, input.types, 1)
        }
    }

    private fun hasReceivedPlatinumBasedDoubletCreator(): FunctionCreator {
        return { HasReceivedPlatinumBasedDoublet() }
    }

    private fun hasHadSomeTreatmentCategoryOfAllTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadSomeTreatmentsWithCategoryOfAllTypes(input.category, input.types, input.integer)
        }
    }

    private fun hasHadFirstLineTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return { HasHadFirstLineTreatmentCategoryOfTypes() }
    }

    private fun hasHadTreatmentCategoryOfTypesWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadTreatmentWithCategoryOfTypesRecently(input.category, input.types, minDate)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadTreatmentWithCategoryButNotOfTypes(input.category, input.types)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringTypesWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadTreatmentWithCategoryButNotOfTypesRecently(input.category, input.types, minDate)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringDrugsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyDrugsInput(function)
            HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(input.category, null, input.drugs)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesIgnoringDrugsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesManyDrugsInput(function)
            HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(input.category, input.types, input.drugs)
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
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
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryOrTypeOneIntegerInput(function)
            val treatment = input.treatment
            if (treatment.mappedType == null) {
                HasHadLimitedTreatmentsWithCategory(treatment.mappedCategory, input.integer)
            } else {
                HasHadLimitedTreatmentsWithCategoryOfTypes(treatment.mappedCategory, setOf(treatment.mappedType!!), input.integer)
            }
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category, input.types, input.integer)
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadLimitedTreatmentsWithCategoryOfTypes(input.category, input.types, input.integer)
        }
    }

    private fun hasHadTreatmentsOfCategoryWithTypesAndStopReasonNotPDCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPD(input.category, input.types, null)
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPDCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPD(input.category, input.types, input.integer)
        }
    }

    private fun hasHadAdjuvantTreatmentWithCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            if (treatment.mappedType == null) {
                HasHadAdjuvantTreatmentWithCategory(treatment.mappedCategory)
            } else {
                HasHadAdjuvantTreatmentWithCategoryOfTypes(setOf(treatment.mappedType!!), treatment.mappedCategory)
            }
        }
    }

    private fun hasHadSystemicTherapyWithIntentsWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyIntentsOneIntegerInput(function)
            val weeksAgo = input.integer
            val minDate = referenceDateProvider().date().minusWeeks(weeksAgo.toLong())
            HasHadSystemicTherapyWithAnyIntent(input.intents, minDate, weeksAgo)
        }
    }

    private fun hasHadSystemicTherapyWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val weeksAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(weeksAgo.toLong())
            HasHadSystemicTherapyWithAnyIntent(null, minDate, weeksAgo)
        }
    }

    private fun hasHadSystemicTherapyWithIntentsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyIntentsInput(function)
            HasHadSystemicTherapyWithAnyIntent(input.intents, null, null)
        }
    }

    private fun hasHadClinicalBenefitFollowingSomeTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneSpecificTreatmentInput(function)
            HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(treatment = input)
        }
    }

    private fun hasHadClinicalBenefitFollowingTreatmentOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            if (input.mappedType == null) {
                HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(category = input.mappedCategory)
            } else {
                HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(category = input.mappedCategory, types = setOf(input.mappedType!!))
            }
        }
    }

    private fun hasHadClinicalBenefitFollowingTreatmentOfCategoryAndTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(category = input.category, types = input.types)
        }
    }

    private fun hasHadSocTargetedTherapyForNsclcCreator(): FunctionCreator {
        return { HasHadSOCTargetedTherapyForNSCLC(emptyList()) }
    }

    private fun hasHadSocTargetedTherapyForNsclcExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyGenesInput(function).geneNames
            HasHadSOCTargetedTherapyForNSCLC(input)
        }
    }

    private fun hasHadTargetedTherapyInterferingWithRasMekMapkPathwayCreator(): FunctionCreator {
        return { HasHadTargetedTherapyInterferingWithRasMekMapkPathway() }
    }

    private fun hasHadRadiotherapyToSomeBodyLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringInput(function)
            HasHadRadiotherapyToSomeBodyLocation(input)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesAsMostRecentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(input.category, input.types)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneSpecificTreatmentInput(function)
            HasHadPDFollowingSpecificTreatment(listOf(treatment))
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
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
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(
                function
            )
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(input.category, input.types, null, null)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
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
        return { function: EligibilityFunction ->
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
        return { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, false)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentWithAnyDrugCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val drugs = functionInputResolver().createManyDrugsInput(function)
            HasHadPDFollowingTreatmentWithAnyDrug(drugs)
        }
    }

    private fun hasAcquiredResistanceToSomeDrugCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasAcquiredResistanceToAnyDrug(functionInputResolver().createManyDrugsInput(function))
        }
    }

    private fun hasRadiologicalProgressionFollowingLatestTreatmentLineCreator(): FunctionCreator {
        return { HasRadiologicalProgressionFollowingLatestTreatmentLine() }
    }

    //TODO: Check implementation
    private fun hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, true)
        }
    }

    private fun hasHadCompleteResectionCreator(): FunctionCreator {
        return { HasHadCompleteResection() }
    }

    private fun hasHadPartialResectionCreator(): FunctionCreator {
        return { HasHadPartialResection() }
    }

    private fun hasHadResectionWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxWeeksAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(maxWeeksAgo.toLong())
            HasHadRecentResection(minDate)
        }
    }

    private fun hasHadLiverResectionCreator(): FunctionCreator {
        return { HasHadLiverResection() }
    }

    private fun hasHadLocalHepaticTherapyWithinWeeksCreator(): FunctionCreator {
        return { HasHadLocalHepaticTherapyWithinWeeks() }
    }

    private fun hasHadIntratumoralInjectionTreatmentCreator(): FunctionCreator {
        return { HasHadIntratumoralInjectionTreatment() }
    }

    private fun hasLimitedCumulativeAnthracyclineExposureCreator(): FunctionCreator {
        return { HasLimitedCumulativeAnthracyclineExposure(doidModel()) }
    }

    private fun hasPreviouslyParticipatedInCurrentTrialCreator(): FunctionCreator {
        return { HasPreviouslyParticipatedInCurrentTrial() }
    }

    private fun hasPreviouslyParticipatedInTrialCreator(): FunctionCreator {
        return { HasPreviouslyParticipatedInTrial() }
    }

    private fun isNotParticipatingInAnotherTrialCreator(): FunctionCreator {
        return { IsNotParticipatingInAnotherTrial() }
    }

    private fun hasReceivedSystemicTherapyforBrainMetastasesCreator(): FunctionCreator {
        return { HasReceivedSystemicTherapyForBrainMetastases() }
    }

    private fun hasHadBrainRadiationTherapyCreator(): FunctionCreator {
        return { HasHadBrainRadiationTherapy() }
    }
}