package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.tumor.HasAcquiredResistanceToAnyDrug
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.medication.MedicationCategories

class TreatmentRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    private val categories = MedicationCategories.create(atcTree())
    private val antiCancerCategories = categories.resolve("Anticancer")

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.IS_NOT_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT to { IsNotEligibleForCurativeTreatment() },
            EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X to isEligibleForOnLabelTreatmentCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_PALLIATIVE_RADIOTHERAPY to { IsEligibleForPalliativeRadiotherapy() },
            EligibilityRule.IS_ELIGIBLE_FOR_LOCO_REGIONAL_THERAPY to { IsEligibleForLocoRegionalTherapy() },
            EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X to isEligibleForTreatmentLinesCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_LOCAL_LIVER_TREATMENT to { IsEligibleForLocalLiverTreatment(doidModel()) },
            EligibilityRule.IS_ELIGIBLE_FOR_INTENSIVE_TREATMENT to { IsEligibleForIntensiveTreatment() },
            EligibilityRule.IS_ELIGIBLE_FOR_FIRST_LINE_PALLIATIVE_CHEMOTHERAPY to {
                IsEligibleForFirstLinePalliativeChemotherapy(
                    HasMetastaticCancer(doidModel())
                )
            },
            EligibilityRule.IS_ELIGIBLE_FOR_LOCAL_TREATMENT_OF_METASTASES to isEligibleForLocalTreatmentOfMetastasesCreator(),
            EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS to hasExhaustedSOCTreatmentsCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES to hasHadSomeApprovedTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES to hasHadSomeSystemicTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES to hasHadLimitedSystemicTreatmentsCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT to hasHadAnyCancerTreatmentCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORY_X to hasHadAnyCancerTreatmentIgnoringSomeCategoryCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_WITHIN_X_MONTHS to hasHadAnyCancerTreatmentWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X to hasHadSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS to hasHadSpecificTreatmentWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_TREATMENT_NAME_X to hasHadFirstLineTreatmentNameCreator(),
            EligibilityRule.HAS_HAD_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z to hasHadSpecificDrugCombinedWithCategoryAndTypesCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X to hasHadTreatmentWithDrugsCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_AS_MOST_RECENT_LINE to hasHadTreatmentWithAnyDrugAsMostRecentCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_WITHIN_Y_WEEKS to hasHadCombinedTreatmentNamesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES to hasHadCombinedTreatmentNamesWithCyclesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT to hasHadTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_WITH_ANY_INTENT_Y to hasHadTreatmentCategoryWithAnyIntentCreator(),
            EligibilityRule.HAS_RECEIVED_PLATINUM_BASED_DOUBLET to { HasReceivedPlatinumBasedDoublet() },
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y to hasHadTreatmentCategoryOfAllTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentCategoryOfAllTypesCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y to { HasHadFirstLineTreatmentCategoryOfTypes() },
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
            EligibilityRule.HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT_WITHIN_Y_WEEKS to hasHadAdjuvantTreatmentWithCategoryWithinWeeksCreator(),
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
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE to
                    { HasRadiologicalProgressionFollowingLatestTreatmentLine() },
            EligibilityRule.HAS_HAD_COMPLETE_RESECTION to { HasHadCompleteResection() },
            EligibilityRule.HAS_HAD_PARTIAL_RESECTION to { HasHadPartialResection() },
            EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS to hasHadResectionWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_LIVER_RESECTION to { HasHadLiverResection() },
            EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS to { HasHadLocalHepaticTherapyWithinWeeks() },
            EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT to { HasHadIntratumoralInjectionTreatment() },
            EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT to
                    { HasLimitedCumulativeAnthracyclineExposure(doidModel()) },
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL to { HasPreviouslyParticipatedInTrial() },
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL_X to hasPreviouslyParticipatedInSpecificTrialCreator(),
            EligibilityRule.IS_NOT_PARTICIPATING_IN_ANOTHER_TRIAL to { IsNotParticipatingInAnotherTrial() },
            EligibilityRule.HAS_RECEIVED_SYSTEMIC_TREATMENT_FOR_BRAIN_METASTASES to { HasReceivedSystemicTherapyForBrainMetastases() },
            EligibilityRule.HAS_HAD_BRAIN_RADIATION_THERAPY to { HasHadBrainRadiationTherapy() },
        )
    }

    private fun isEligibleForOnLabelTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatmentName = functionInputResolver().createOneSpecificTreatmentInput(function)
            IsEligibleForOnLabelTreatment(treatmentName, RecommendationEngineFactory(resources))
        }
    }

    private fun isEligibleForLocalTreatmentOfMetastasesCreator(): FunctionCreator {
        return { IsEligibleForLocalTreatmentOfMetastases(HasMetastaticCancer(doidModel())) }
    }

    private fun isEligibleForTreatmentLinesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val lines = functionInputResolver().createManyIntegersInput(function)
            IsEligibleForTreatmentLines(lines)
        }
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
        return { HasHadAnyCancerTreatment(null, antiCancerCategories) }
    }

    private fun hasHadAnyCancerTreatmentIgnoringSomeCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            HasHadAnyCancerTreatment(treatment.mappedCategory, antiCancerCategories)
        }
    }

    private fun hasHadAnyCancerTreatmentWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val monthsAgo = functionInputResolver().createOneIntegerInput(function)
            val interpreter = createInterpreterForWashout(null, monthsAgo)
            val minDate = referenceDateProvider().date().minusMonths(monthsAgo.toLong())
            HasHadAnyCancerTreatmentSinceDate(minDate, monthsAgo, antiCancerCategories, interpreter)
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

    private fun hasHadTreatmentWithCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            treatment.mappedType?.let { mappedType ->
                HasHadSomeTreatmentsWithCategoryOfTypes(treatment.mappedCategory, setOf(mappedType), 1)
            } ?: HasHadSomeTreatmentsWithCategory(treatment.mappedCategory, 1)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadSomeTreatmentsWithCategoryOfTypes(input.category, input.types, 1)
        }
    }

    private fun hasHadTreatmentCategoryWithAnyIntentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyIntentsInput(function)
            HasHadSomeTreatmentsWithCategoryWithIntents(input.category, input.intents)
        }
    }

    private fun hasHadTreatmentCategoryOfAllTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadSomeTreatmentsWithCategoryOfAllTypes(input.category, input.types, 1)
        }
    }

    private fun hasHadSomeTreatmentCategoryOfAllTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadSomeTreatmentsWithCategoryOfAllTypes(input.category, input.types, input.integer)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            val interpreter = createInterpreterForWashout(input.integer, null)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadTreatmentWithCategoryOfTypesRecently(input.category, input.types, minDate, interpreter)
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
            val interpreter = createInterpreterForWashout(input.integer, null)
            val minDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasHadTreatmentWithCategoryButNotOfTypesRecently(input.category, input.types, minDate, interpreter)
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
            treatment.mappedType?.let { mappedType ->
                HasHadSomeTreatmentsWithCategoryOfTypes(treatment.mappedCategory, setOf(mappedType), input.integer)
            } ?: HasHadSomeTreatmentsWithCategory(treatment.mappedCategory, input.integer)
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneTreatmentCategoryOrTypeOneIntegerInput(function)
            val treatment = input.treatment
            treatment.mappedType?.let { mappedType ->
                HasHadLimitedTreatmentsWithCategoryOfTypes(treatment.mappedCategory, setOf(mappedType), input.integer)
            } ?: HasHadLimitedTreatmentsWithCategory(treatment.mappedCategory, input.integer)
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
            treatment.mappedType?.let { mappedType ->
                HasHadAdjuvantTreatmentWithCategoryOfTypes(setOf(mappedType), treatment.mappedCategory)
            } ?: HasHadAdjuvantTreatmentWithCategory(treatment.mappedCategory, null, null)
        }
    }

    private fun hasHadAdjuvantTreatmentWithCategoryWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (treatment, weeksAgo) = functionInputResolver().createOneTreatmentCategoryOrTypeOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(weeksAgo.toLong())
            HasHadAdjuvantTreatmentWithCategory(treatment.mappedCategory, minDate, weeksAgo)
        }
    }

    private fun hasHadSystemicTherapyWithIntentsWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (intents, weeksAgo) = functionInputResolver().createManyIntentsOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(weeksAgo.toLong())
            HasHadSystemicTherapyWithAnyIntent(intents, minDate, weeksAgo)
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
            input.mappedType?.let { mappedType ->
                HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(category = input.mappedCategory, types = setOf(mappedType))
            } ?: HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(category = input.mappedCategory)
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
            HasHadRadiotherapyToSomeBodyLocation(functionInputResolver().createOneStringInput(function))
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
            HasHadPDFollowingSpecificTreatment(listOf(functionInputResolver().createOneSpecificTreatmentInput(function)))
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = functionInputResolver().createOneTreatmentCategoryOrTypeInput(function)
            treatment.mappedType?.let { mappedType ->
                HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(treatment.mappedCategory, setOf(mappedType), null, null)
            } ?: HasHadPDFollowingTreatmentWithCategory(treatment.mappedCategory)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (category, types) = functionInputResolver().createOneTreatmentCategoryManyTypesInput(function)
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(category, types, null, null)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (category, types, cycles) = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(category, types, cycles, null)
        }
    }

    //TODO: Check implementation
    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (category, types, weeks) = functionInputResolver().createOneTreatmentCategoryManyTypesOneIntegerInput(function)
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(category, types, null, weeks)
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
            HasHadPDFollowingTreatmentWithAnyDrug(functionInputResolver().createManyDrugsInput(function))
        }
    }

    private fun hasAcquiredResistanceToSomeDrugCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasAcquiredResistanceToAnyDrug(functionInputResolver().createManyDrugsInput(function))
        }
    }

    //TODO: Check implementation
    private fun hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minSystemicTreatments = functionInputResolver().createOneIntegerInput(function)
            HasHadPDFollowingSomeSystemicTreatments(minSystemicTreatments, true)
        }
    }

    private fun hasHadResectionWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxWeeksAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusWeeks(maxWeeksAgo.toLong())
            HasHadRecentResection(minDate)
        }
    }

    private fun hasPreviouslyParticipatedInSpecificTrialCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasPreviouslyParticipatedInTrial(functionInputResolver().createOneStringInput(function))
        }
    }

    private fun createInterpreterForWashout(inputWeeks: Int?, inputMonths: Int?): MedicationStatusInterpreter {
        val minDate =
            when {
                inputWeeks != null && inputMonths == null -> {
                    referenceDateProvider().date().minusWeeks(inputWeeks.toLong()).plusWeeks(2)
                }

                inputMonths != null && inputWeeks == null -> {
                    referenceDateProvider().date().minusMonths(inputMonths.toLong()).plusWeeks(2)
                }

                else -> {
                    throw IllegalArgumentException("Exactly one of inputWeeks or inputMonths must be provided")
                }
            }

        return MedicationStatusInterpreterOnEvaluationDate(minDate)
    }
}