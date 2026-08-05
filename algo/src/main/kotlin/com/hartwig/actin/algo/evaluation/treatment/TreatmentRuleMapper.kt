package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.And
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate.Companion.createInterpreterForWashout
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.datamodel.trial.DrugParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.IntentParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyGenesParameter
import com.hartwig.actin.datamodel.trial.ManyIntegersParameter
import com.hartwig.actin.datamodel.trial.ManyIntentsParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentCategoriesParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentTypesParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentsParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.StringParameter
import com.hartwig.actin.datamodel.trial.TreatmentCategoryOrTypeParameter
import com.hartwig.actin.datamodel.trial.TreatmentCategoryParameter
import com.hartwig.actin.datamodel.trial.TreatmentParameter
import com.hartwig.actin.datamodel.trial.TreatmentResponseParameter
import com.hartwig.actin.datamodel.trial.TreatmentTypeParameter
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.trial.input.EligibilityRule

class TreatmentRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    private val categories = MedicationCategories.create(atcTree)
    private val antiCancerCategories = categories.resolve("Anticancer")
    private val selector: MedicationSelector =
        MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider.date(), null))
    private val referenceDate = referenceDateProvider.date()

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.IS_NOT_ELIGIBLE_FOR_TREATMENT_WITH_CURATIVE_INTENT to { IsNotEligibleForCurativeTreatment(evaluationLabels.treatment) },
            EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X to isEligibleForOnLabelTreatmentCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X_WITH_INTENT_Y to isEligibleForOnLabelTreatmentWithIntentCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_RADIOTHERAPY to { IsEligibleForRadiotherapy(labels = evaluationLabels.treatment) },
            EligibilityRule.IS_ELIGIBLE_FOR_RADIOTHERAPY_TO_BODY_LOCATION_X to isEligibleForRadiotherapyToBodyLocationCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_PALLIATIVE_RADIOTHERAPY to { IsEligibleForPalliativeRadiotherapy(evaluationLabels.treatment) },
            EligibilityRule.IS_ELIGIBLE_FOR_LOCO_REGIONAL_THERAPY to { IsEligibleForLocoRegionalTherapy(evaluationLabels.treatment) },
            EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_LINES_X to isEligibleForTreatmentLinesCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_LOCAL_LIVER_TREATMENT to { IsEligibleForLocalLiverTreatment(doidModel, evaluationLabels.treatment) },
            EligibilityRule.IS_ELIGIBLE_FOR_INTENSIVE_TREATMENT to { IsEligibleForIntensiveTreatment(evaluationLabels.treatment) },
            EligibilityRule.IS_ELIGIBLE_FOR_FIRST_LINE_PALLIATIVE_CHEMOTHERAPY to {
                IsEligibleForFirstLinePalliativeChemotherapy(
                    HasMetastaticCancer(doidModel, evaluationLabels.tumor), evaluationLabels.treatment
                )
            },
            EligibilityRule.IS_ELIGIBLE_FOR_LOCAL_TREATMENT_OF_METASTASES to isEligibleForLocalTreatmentOfMetastasesCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_SURGERY_TYPE_X to isEligibleForSpecificSurgeryCreator(),
            EligibilityRule.IS_ELIGIBLE_FOR_TREATMENT_OF_CATEGORY_X_AND_ANY_TYPE_Y to isEligibleForTreatmentOfCategoryAndTypeCreator(),
            EligibilityRule.MEETS_SPECIFIC_CRITERIA_FOR_RESECTION to { MeetsSpecificCriteriaForResection(evaluationLabels.treatment) },
            EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS to hasExhaustedSOCTreatmentsCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_APPROVED_TREATMENT_LINES to hasHadSomeApprovedTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES to hasHadSomeSystemicTreatmentCreator(),
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES to hasHadLimitedSystemicTreatmentsCreator(),
            EligibilityRule.HAS_HAD_AT_LEAST_X_SYSTEMIC_TREATMENT_LINES_ONLY_INCLUDING_NEO_OR_ADJUVANT_IF_NEXT_LINE_WITHIN_Y_MONTHS to hasHadSomeSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES_ONLY_INCLUDING_NEO_OR_ADJUVANT_IF_NEXT_LINE_WITHIN_Y_MONTHS to hasHadLimitedSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT to hasHadAnyCancerTreatmentCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORIES_X to hasHadAnyCancerTreatmentIgnoringCategoriesCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_IGNORING_CATEGORY_X_OF_TYPES_Y_WITHIN_Z_MONTHS to hasHadAnyCancerTreatmentIgnoringTypesWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_ANY_CANCER_TREATMENT_WITHIN_X_MONTHS to hasHadAnyCancerTreatmentWithinMonthsCreator(),
            EligibilityRule.HAS_HAD_ANY_SYSTEMIC_CANCER_TREATMENT_WITHIN_X_MONTHS to hasHadAnyCancerTreatmentWithinMonthsCreator(true),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X to hasHadSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS to hasHadSpecificTreatmentWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_FOR_AT_MOST_Y_WEEKS to hasHadLimitedWeeksOfSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_FOR_AT_LEAST_Y_WEEKS to hasHadSufficientWeeksOfSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_DOSE_REDUCTION_DURING_TREATMENT_NAME_X to hasHadSpecificTreatmentAndDoseReductionCreator(),
            EligibilityRule.HAS_HAD_DOSE_REDUCTION_DURING_TREATMENT_WITH_DRUG_X to hasHadTreatmentWithDrugAndDoseReductionCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_SYSTEMIC_TREATMENT_NAME_X to hasHadFirstLineSystemicTreatmentNameCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_SYSTEMIC_TREATMENT_NAME_X_WITHOUT_PROGRESSION_AND_AT_LEAST_Y_CYCLES to hasHadFirstLineTreatmentNameWithoutPdAndWithCyclesCreator(),
            EligibilityRule.HAS_HAD_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z to hasHadSpecificDrugCombinedWithCategoryAndTypesCreator(),
            EligibilityRule.HAS_HAD_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z_AS_A_LINE to hasHadSpecificDrugCombinedWithCategoryAndTypesAsLineCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_COMBINED_WITH_CATEGORY_Z_TREATMENT_OF_TYPES_A to hasHadCategoryAndTypesCombinedWithCategoryAndTypesCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X to hasHadTreatmentWithAnyDrugCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_AS_MOST_RECENT_LINE to hasHadTreatmentWithAnyDrugAsMostRecentCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X_AND_AT_LEAST_Y_CYCLES to hasHadTreatmentWithAnyDrugWithCyclesCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES to hasHadCombinedTreatmentNamesWithCyclesCreator(),
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_AND_AT_LEAST_Y_CYCLES to hasHadTreatmentNameWithCyclesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT to hasHadTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadTreatmentCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_WITH_ANY_INTENT_Y to hasHadTreatmentCategoryWithAnyIntentCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_WITH_ANY_INTENT_Y_WITHIN_Z_MONTHS to hasHadTreatmentCategoryWithAnyIntentRecentlyCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ANY_TYPE_Y_WITH_ANY_INTENT_Z to hasHadTreatmentCategoryAndTypeWithAnyIntentCreator(),
            EligibilityRule.HAS_RECEIVED_PLATINUM_BASED_DOUBLET to { HasReceivedPlatinumBasedDoublet(doidModel, evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y to hasHadTreatmentCategoryOfAllTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ALL_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentCategoryOfAllTypesCreator(),
            EligibilityRule.HAS_HAD_FIRST_LINE_CATEGORY_X_TREATMENT_OF_TYPES_Y to { HasHadFirstLineTreatmentCategoryOfTypes(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_WITHIN_Z_WEEKS to hasHadTreatmentCategoryWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITHIN_Z_WEEKS to hasHadTreatmentCategoryOfTypesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y to hasHadTreatmentCategoryIgnoringTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_TYPES_Y_WITHIN_Z_WEEKS to hasHadTreatmentCategoryIgnoringTypesWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_DRUGS_Y to hasHadTreatmentCategoryIgnoringDrugsCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_IGNORING_DRUGS_Z to hasHadTreatmentCategoryOfTypesIgnoringDrugsCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_LEAST_Y_LINES to hasHadSomeTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_AND_AT_MOST_Y_LINES to hasHadLimitedTreatmentsOfCategoryCreator(true),
            EligibilityRule.HAS_NOT_HAD_CATEGORY_X_TREATMENT_OR_AT_MOST_Y_LINES to hasHadLimitedTreatmentsOfCategoryCreator(false),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_LINES to hasHadSomeTreatmentsOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_MOST_Z_LINES to hasHadLimitedTreatmentsOfCategoryWithTypesCreator(
                true
            ),
            EligibilityRule.HAS_NOT_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_OR_AT_MOST_Z_LINES to hasHadLimitedTreatmentsOfCategoryWithTypesCreator(
                false
            ),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_WITH_STOP_REASON_OTHER_THAN_PD to hasHadTreatmentsOfCategoryWithTypesAndStopReasonNotPDCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_FOR_AT_MOST_Z_WEEKS_WITH_STOP_REASON_OTHER_THAN_PD
                    to hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_FOR_AT_MOST_Z_WEEKS to hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPES_Y_FOR_AT_LEAST_Z_WEEKS to hasHadSufficientWeeksOfTreatmentOfCategoryWithTypesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y_AS_MOST_RECENT_LINE to hasHadTreatmentCategoryOfTypesAsMostRecentCreator(),
            EligibilityRule.HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT to hasHadAdjuvantTreatmentWithCategoryCreator(),
            EligibilityRule.HAS_HAD_ADJUVANT_CATEGORY_X_TREATMENT_WITHIN_Y_WEEKS to hasHadAdjuvantTreatmentWithCategoryWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X_WITHIN_Y_WEEKS to hasHadSystemicTherapyWithIntentsWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X_AT_LEAST_Y_WEEKS_AGO to hasHadSystemicTherapyWithIntentsAtLeastWeeksAgoCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_THERAPY_WITH_ANY_INTENT_X to hasHadSystemicTherapyWithIntentsCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_TREATMENT_IN_METASTATIC_SETTING to {
                HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSetting(
                    referenceDate = referenceDate,
                    intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
                    categoryToIgnore = null,
                    settingDescription = evaluationLabels.treatment.descriptionMetastatic(),
                    labels = evaluationLabels.treatment
                )
            },
            EligibilityRule.HAS_HAD_SYSTEMIC_TREATMENT_IN_METASTATIC_SETTING_EXCLUDING_CATEGORY_X to hasHadSystemicTreatmentInMetastaticSettingExcludingCategoryCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_TREATMENT_IN_ADVANCED_OR_METASTATIC_SETTING to {
                HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSetting(
                    referenceDate = referenceDate,
                    intentsToIgnore = setOf(Intent.CURATIVE),
                    categoryToIgnore = null,
                    settingDescription = evaluationLabels.treatment.descriptionAdvancedOrMetastatic(),
                    labels = evaluationLabels.treatment
                )
            },
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES_IN_THE_METASTATIC_SETTING to hasHadLimitedSystemicTreatmentsInTheMetastaticSettingCreator(),
            EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES_IN_THE_ADVANCED_OR_METASTATIC_SETTING to hasHadLimitedSystemicTreatmentsInTheAdvancedOrMetastaticSettingCreator(),
            EligibilityRule.HAS_HAD_RESPONSE_X_FOLLOWING_CATEGORY_Y_TREATMENT_OF_TYPES_Z to hasHadResponseFollowingTreatmentOfCategoryAndTypesCreator(),
            EligibilityRule.HAS_HAD_RESPONSE_X_FOLLOWING_ANY_TREATMENT_NAME_X to hasHadResponseFollowingSomeSpecificTreatmentCreator(),
            EligibilityRule.HAS_HAD_RADIOLOGICAL_RESPONSE_TO_TREATMENT_WITH_DRUG_X to hasHadRadiologicalResponseFollowingDrugTreatmentCreator(),
            EligibilityRule.HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_TREATMENT_WITH_ANY_NAME_X to hasHadClinicalBenefitFollowingSomeTreatmentCreator(),
            EligibilityRule.HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_CATEGORY_X_TREATMENT to hasHadClinicalBenefitFollowingTreatmentOfCategoryCreator(),
            EligibilityRule.HAS_HAD_OBJECTIVE_CLINICAL_BENEFIT_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasHadClinicalBenefitFollowingTreatmentOfCategoryAndTypesCreator(),
            EligibilityRule.HAS_HAD_SYSTEMIC_TREATMENT_ONLY_OF_CATEGORY_X_AND_TYPE_Y to hasHadSystemicTreatmentOnlyOfCategoryOfTypesCreator(),
            EligibilityRule.HAS_HAD_SOC_TARGETED_THERAPY_FOR_NSCLC to hasHadSocTargetedTherapyForNsclcCreator(),
            EligibilityRule.HAS_HAD_SOC_TARGETED_THERAPY_FOR_NSCLC_EXCLUDING_DRIVER_GENES_X to hasHadSocTargetedTherapyForNsclcExcludingSomeGenesCreator(),
            EligibilityRule.HAS_HAD_TARGETED_THERAPY_INTERFERING_WITH_RAS_MEK_MAPK_PATHWAY to hasHadTargetedTherapyInterferingWithRasMekMapkPathwayCreator(),
            EligibilityRule.HAS_HAD_NON_INTERNAL_RADIOTHERAPY to { HasHadNonInternalRadiotherapy(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_RADIOTHERAPY_TO_BODY_LOCATION_X to hasHadRadiotherapyToSomeBodyLocationCreator(),
            EligibilityRule.HAS_HAD_RADIOTHERAPY_TO_BODY_LOCATION_X_FOR_AT_LEAST_Y_LINES to hasHadRadiotherapyToSomeBodyLocationAndSufficientLinesCreator(),
            EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_ONLY_TYPES_Y_FOR_AT_LEAST_Z_MONTHS_AS_MOST_RECENT_LINE to hasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecentCreator(),
            EligibilityRule.HAS_HAD_CHEMORADIOTHERAPY_WITH_ANY_DRUG_X_AND_AT_LEAST_Y_CYCLES to hasHadChemoradiotherapyWithAnyDrugAndMinimumCyclesCreator(),
            EligibilityRule.HAS_HAD_CHEMORADIOTHERAPY_OF_TYPE_X_CHEMOTHERAPY_AND_AT_LEAST_Y_CYCLES to hasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentNameCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_WEEKS to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_CATEGORY_X_TREATMENT_OF_TYPES_Y_AND_AT_LEAST_Z_CYCLES to hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_TREATMENT_WITH_ANY_DRUG_X to hasProgressiveDiseaseFollowingTreatmentWithAnyDrugCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_DRUG_X_COMBINED_WITH_CATEGORY_Y_TREATMENT_OF_TYPES_Z_FOR_AT_LEAST_A_WEEKS to hasProgressiveDiseaseFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksCreator(),
            EligibilityRule.HAS_ACQUIRED_RESISTANCE_TO_ANY_DRUG_X to hasAcquiredResistanceToSomeDrugCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_FOLLOWING_AT_LEAST_X_TREATMENT_LINES to hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(),
            EligibilityRule.HAS_RADIOLOGICAL_PROGRESSIVE_DISEASE_AFTER_LATEST_TREATMENT_LINE to
                    { HasHadProgressionFollowingLatestTreatmentLine(labels = evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_DEFINITIVE_LOCOREGIONAL_THERAPY_WITH_CURATIVE_INTENT to { HasHadDefinitiveLocoregionalTherapyWithCurativeIntent(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_COMPLETE_RESECTION to { HasHadCompleteResection(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_PARTIAL_RESECTION to { HasHadPartialResection(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_RESECTION_WITHIN_X_WEEKS to hasHadResectionWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_LIVER_RESECTION to { HasHadLiverResection(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_LOCAL_HEPATIC_THERAPY_WITHIN_X_WEEKS to { HasHadLocalHepaticTherapyWithinWeeks(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_INTRATUMORAL_INJECTION_TREATMENT to { HasHadIntratumoralInjectionTreatment(evaluationLabels.treatment) },
            EligibilityRule.HAS_CUMULATIVE_ANTHRACYCLINE_EXPOSURE_OF_AT_MOST_X_MG_PER_M2_DOXORUBICIN_OR_EQUIVALENT to
                    { HasLimitedCumulativeAnthracyclineExposure(doidModel, evaluationLabels.treatment) },
            EligibilityRule.HAS_PATHOLOGICAL_COMPLETE_RESPONSE_AFTER_SURGERY to { HasPathologicalCompleteResponseAfterSurgery(evaluationLabels.treatment) },
            EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL_WITH_ACRONYM_X to hasPreviouslyParticipatedInSpecificTrialCreator(),
            EligibilityRule.IS_NOT_PARTICIPATING_IN_ANOTHER_INTERVENTIONAL_TRIAL to {
                IsNotParticipatingInAnotherInterventionalTrial(
                    selector,
                    referenceDateProvider.date().minusWeeks(4),
                    evaluationLabels.treatment
                )
            },
            EligibilityRule.HAS_RECEIVED_SYSTEMIC_TREATMENT_FOR_BRAIN_METASTASES to { HasReceivedSystemicTherapyForBrainMetastases(evaluationLabels.treatment) },
            EligibilityRule.HAS_HAD_BRAIN_RADIATION_THERAPY to { HasHadBrainRadiationTherapy(evaluationLabels.treatment) },
            EligibilityRule.IS_PLATINUM_RESISTANT to { IsPlatinumResistant(referenceDate, evaluationLabels.treatment) },
            EligibilityRule.IS_PLATINUM_SENSITIVE to { IsPlatinumSensitive(referenceDate, evaluationLabels.treatment) },
            EligibilityRule.IS_PRIMARY_PLATINUM_REFRACTORY_WITHIN_X_MONTHS to isPrimaryPlatinumRefractoryWithinMonthsCreator(),
        )
    }

    private fun isEligibleForOnLabelTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatmentName = function.param<TreatmentParameter>(0).value
            val minDate = referenceDate.minusWeeks(26)
            IsEligibleForOnLabelTreatment(
                treatmentName, StandardOfCareEvaluatorFactory(resources), doidModel, minDate,
                molecularLabels = evaluationLabels.molecular, treatmentLabels = evaluationLabels.treatment
            )
        }
    }

    private fun isEligibleForOnLabelTreatmentWithIntentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatmentName = function.param<TreatmentParameter>(0).value
            val minDate = referenceDate.minusWeeks(26)
            val intent = function.param<IntentParameter>(1).value
            IsEligibleForOnLabelTreatment(
                treatmentName, StandardOfCareEvaluatorFactory(resources), doidModel, minDate, intent = intent,
                molecularLabels = evaluationLabels.molecular, treatmentLabels = evaluationLabels.treatment
            )
        }
    }

    private fun isEligibleForRadiotherapyToBodyLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val bodyLocation = function.param<StringParameter>(0).value
            IsEligibleForRadiotherapy(bodyLocation, evaluationLabels.treatment)
        }
    }

    private fun isEligibleForLocalTreatmentOfMetastasesCreator(): FunctionCreator {
        return { IsEligibleForLocalTreatmentOfMetastases(HasMetastaticCancer(doidModel, evaluationLabels.tumor), evaluationLabels.treatment) }
    }

    private fun isEligibleForTreatmentLinesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val lines = function.param<ManyIntegersParameter>(0).value
            IsEligibleForTreatmentLines(lines, evaluationLabels.treatment)
        }
    }

    private fun isEligibleForSpecificSurgeryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = function.param<StringParameter>(0).value
            IsEligibleForSpecificSurgery(input, evaluationLabels.treatment)
        }
    }

    private fun isEligibleForTreatmentOfCategoryAndTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            IsEligibleForTreatmentOfCategoryAndType(category, types, evaluationLabels.treatment)
        }
    }

    private fun hasExhaustedSOCTreatmentsCreator(): FunctionCreator {
        return { HasExhaustedSOCTreatments(StandardOfCareEvaluatorFactory(resources), doidModel, evaluationLabels.treatment) }
    }

    private fun hasHadSomeApprovedTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minApprovedTreatments = function.param<IntegerParameter>(0).value
            HasHadSomeApprovedTreatments(minApprovedTreatments, evaluationLabels.treatment)
        }
    }

    private fun hasHadSomeSystemicTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minSystemicTreatments = function.param<IntegerParameter>(0).value
            HasHadSomeSystemicTreatments(minSystemicTreatments, evaluationLabels.treatment)
        }
    }

    private fun hasHadLimitedSystemicTreatmentsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxSystemicTreatments = function.param<IntegerParameter>(0).value
            HasHadLimitedSystemicTreatments(maxSystemicTreatments, evaluationLabels.treatment)
        }
    }

    private fun hasHadSomeSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minSystemicTreatments = function.param<IntegerParameter>(0).value
            val maxMonthsBeforeNextLine = function.param<IntegerParameter>(1).value
            HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths.createForMinimumTreatmentLines(
                minSystemicTreatments,
                maxMonthsBeforeNextLine,
                referenceDate,
                evaluationLabels.treatment
            )
        }
    }

    private fun hasHadLimitedSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxSystemicTreatments = function.param<IntegerParameter>(0).value
            val maxMonthsBeforeNextLine = function.param<IntegerParameter>(1).value
            HasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonths.createForMaximumTreatmentLines(
                maxSystemicTreatments,
                maxMonthsBeforeNextLine,
                referenceDate,
                evaluationLabels.treatment
            )
        }
    }

    private fun hasHadAnyCancerTreatmentCreator(): FunctionCreator {
        return { HasHadAnyCancerTreatment(emptySet(), antiCancerCategories, evaluationLabels.treatment) }
    }

    private fun hasHadAnyCancerTreatmentIgnoringCategoriesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatmentCategories = function.param<ManyTreatmentCategoriesParameter>(0).value
            HasHadAnyCancerTreatment(treatmentCategories, antiCancerCategories, evaluationLabels.treatment)
        }
    }

    private fun hasHadAnyCancerTreatmentIgnoringTypesWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val categoryToIgnore = function.param<TreatmentCategoryParameter>(0).value
            val typesToIgnore = function.param<ManyTreatmentTypesParameter>(1).value
            val monthsAgo = function.param<IntegerParameter>(2).value
            val (interpreter, minDate) = createInterpreterForWashout(null, monthsAgo, referenceDate)
            HasHadAnyCancerTreatmentSinceDate(
                minDate,
                monthsAgo,
                antiCancerCategories,
                interpreter,
                categoryToIgnore,
                typesToIgnore,
                false,
                evaluationLabels.treatment
            )
        }
    }

    private fun hasHadAnyCancerTreatmentWithinMonthsCreator(onlySystemicTreatments: Boolean = false): FunctionCreator {
        return { function: EligibilityFunction ->
            val monthsAgo = function.param<IntegerParameter>(0).value
            val (interpreter, minDate) = createInterpreterForWashout(null, monthsAgo, referenceDate)
            HasHadAnyCancerTreatmentSinceDate(
                minDate,
                monthsAgo,
                antiCancerCategories,
                interpreter,
                null,
                emptySet(),
                onlySystemicTreatments,
                evaluationLabels.treatment
            )
        }
    }

    private fun hasHadLimitedWeeksOfSpecificTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentParameter>(0).value
            val weeks = function.param<IntegerParameter>(1).value
            HasHadLimitedWeeksOfSpecificTreatment(treatment, weeks, evaluationLabels.treatment)
        }
    }

    private fun hasHadSufficientWeeksOfSpecificTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentParameter>(0).value
            val weeks = function.param<IntegerParameter>(1).value
            HasHadSufficientWeeksOfSpecificTreatment(treatment, weeks, evaluationLabels.treatment)
        }
    }

    private fun hasHadSpecificTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentParameter>(0).value
            HasHadLimitedWeeksOfSpecificTreatment(treatment, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadSpecificTreatmentWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT, Parameter.Type.INTEGER)
            val treatment = function.param<TreatmentParameter>(0).value
            val weeksAgo = function.param<IntegerParameter>(1).value
            val minDate = referenceDate.minusWeeks(weeksAgo.toLong())
            HasHadSpecificTreatmentSinceDate(treatment, minDate, evaluationLabels.treatment)
        }
    }

    private fun hasHadSpecificTreatmentAndDoseReductionCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentParameter>(0).value
            HasHadSomeSpecificTreatmentsWithDoseReduction(treatment, evaluationLabels.treatment)
        }
    }

    private fun hasHadFirstLineSystemicTreatmentNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentParameter>(0).value
            HasHadSpecificFirstLineSystemicTreatment(treatment, evaluationLabels.treatment)
        }
    }

    private fun hasHadFirstLineTreatmentNameWithoutPdAndWithCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT, Parameter.Type.INTEGER)
            val treatment = function.param<TreatmentParameter>(0).value
            val minCycles = function.param<IntegerParameter>(1).value
            HasHadSystemicFirstLineTreatmentWithoutPdAndWithCycles(treatment, minCycles = minCycles, labels = evaluationLabels.treatment)
        }
    }

    private fun hasHadSpecificDrugCombinedWithCategoryAndTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.DRUG,
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES
            )
            val drug = function.param<DrugParameter>(0).value
            val category = function.param<TreatmentCategoryParameter>(1).value
            val types = function.param<ManyTreatmentTypesParameter>(2).value
            HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(drug, category, types, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadSpecificDrugCombinedWithCategoryAndTypesAsLineCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.DRUG,
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val drug = function.param<DrugParameter>(0).value
            val category = function.param<TreatmentCategoryParameter>(1).value
            val types = function.param<ManyTreatmentTypesParameter>(2).value
            val line = function.param<IntegerParameter>(3).value
            HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(drug, category, types, line, evaluationLabels.treatment)
        }
    }

    private fun hasHadCategoryAndTypesCombinedWithCategoryAndTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES
            )
            val category1 = function.param<TreatmentCategoryParameter>(0).value
            val types1 = function.param<ManyTreatmentTypesParameter>(1).value
            val category2 = function.param<TreatmentCategoryParameter>(2).value
            val types2 = function.param<ManyTreatmentTypesParameter>(3).value
            HasHadCategoryAndTypesCombinedWithOtherCategoryAndTypes(category1, types1, category2, types2, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentWithAnyDrugCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val drugs = function.param<ManyDrugsParameter>(0).value
            HasHadTreatmentWithDrugAndCycles(drugs, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentWithAnyDrugWithCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_DRUGS, Parameter.Type.INTEGER)
            val drugs = function.param<ManyDrugsParameter>(0).value
            val cycles = function.param<IntegerParameter>(1).value
            HasHadTreatmentWithDrugAndCycles(drugs, cycles, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentWithDrugAndDoseReductionCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.DRUG)
            val drug = function.param<DrugParameter>(0).value
            HasHadTreatmentWithDrugAndDoseReduction(drug, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentWithAnyDrugAsMostRecentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val drugs = function.param<ManyDrugsParameter>(0).value
            HasHadTreatmentWithDrugFromSetAsMostRecent(drugs, evaluationLabels.treatment)
        }
    }

    private fun hasHadCombinedTreatmentNamesWithCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_TREATMENTS, Parameter.Type.INTEGER, Parameter.Type.INTEGER)
            val treatments = function.param<ManyTreatmentsParameter>(0).value
            val minCycles = function.param<IntegerParameter>(1).value
            val maxCycles = function.param<IntegerParameter>(2).value
            HasHadCombinedTreatmentNamesWithCycles(treatments, minCycles, maxCycles, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentNameWithCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT, Parameter.Type.INTEGER)
            val treatment = function.param< TreatmentParameter>(0).value
            val minCycles = function.param<IntegerParameter>(1).value
            HasHadCombinedTreatmentNamesWithCycles(listOf(treatment), minCycles, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentWithCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentCategoryOrTypeParameter>(0).value
            treatment.type?.let { mappedType ->
                HasHadSomeTreatmentsWithCategoryOfTypes(treatment.category, setOf(mappedType), 1, evaluationLabels.treatment)
            } ?: HasHadSomeTreatmentsWithCategory(treatment.category, 1, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadSomeTreatmentsWithCategoryOfTypes(category, types, 1, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryWithAnyIntentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_INTENTS)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val intents = function.param<ManyIntentsParameter>(1).value
            HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(category, intents, labels = evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryWithAnyIntentRecentlyCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_INTENTS,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val intents = function.param<ManyIntentsParameter>(1).value
            val weeksAgo = function.param<IntegerParameter>(2).value
            val minDate = createInterpreterForWashout(weeksAgo, null, referenceDate).second
            HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(category, intents, minDate = minDate, labels = evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryAndTypeWithAnyIntentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.MANY_INTENTS
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val intents = function.param<ManyIntentsParameter>(2).value

            HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(category, intents, allowedTypes = types, labels = evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryOfAllTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadSomeTreatmentsWithCategoryOfAllTypes(category, types, 1, evaluationLabels.treatment)
        }
    }

    private fun hasHadSomeTreatmentCategoryOfAllTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val lines = function.param<IntegerParameter>(2).value
            HasHadSomeTreatmentsWithCategoryOfAllTypes(category, types, lines, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY_OR_TYPE, Parameter.Type.INTEGER)
            val treatment = function.param<TreatmentCategoryOrTypeParameter>(0).value
            val weeksAgo = function.param<IntegerParameter>(1).value
            val (interpreter, minDate) = createInterpreterForWashout(weeksAgo, null, referenceDate)
            treatment.type?.let { mappedType ->
                HasHadTreatmentWithCategoryOfTypesRecently(treatment.category, setOf(mappedType), minDate, interpreter, evaluationLabels.treatment)
            } ?: HasHadTreatmentWithCategoryOfTypesRecently(treatment.category, null, minDate, interpreter, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val weeksAgo = function.param<IntegerParameter>(2).value
            val (interpreter, minDate) = createInterpreterForWashout(weeksAgo, null, referenceDate)
            HasHadTreatmentWithCategoryOfTypesRecently(category, types, minDate, interpreter, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadTreatmentWithCategoryButNotOfTypes(category, types, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringTypesWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val weeksAgo = function.param<IntegerParameter>(2).value
            val (interpreter, minDate) = createInterpreterForWashout(weeksAgo, null, referenceDate)
            HasHadTreatmentWithCategoryButNotOfTypesRecently(category, types, minDate, interpreter, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryIgnoringDrugsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_DRUGS)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val drugs = function.param<ManyDrugsParameter>(1).value
            HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(category, null, drugs, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesIgnoringDrugsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.MANY_DRUGS
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val drugs = function.param<ManyDrugsParameter>(2).value
            HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(category, types, drugs, evaluationLabels.treatment)
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY_OR_TYPE, Parameter.Type.INTEGER)
            val treatment = function.param<TreatmentCategoryOrTypeParameter>(0).value
            val lines = function.param<IntegerParameter>(1).value
            treatment.type?.let { mappedType ->
                HasHadSomeTreatmentsWithCategoryOfTypes(treatment.category, setOf(mappedType), lines, evaluationLabels.treatment)
            } ?: HasHadSomeTreatmentsWithCategory(treatment.category, lines, evaluationLabels.treatment)
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryCreator(treatmentIsRequired: Boolean): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY_OR_TYPE, Parameter.Type.INTEGER)
            val treatment = function.param<TreatmentCategoryOrTypeParameter>(0).value
            val lines = function.param<IntegerParameter>(1).value
            treatment.type?.let { mappedType ->
                HasHadLimitedTreatmentsWithCategoryOfTypes(treatment.category, setOf(mappedType), lines, treatmentIsRequired, evaluationLabels.treatment)
            } ?: HasHadLimitedTreatmentsWithCategoryOfTypes(treatment.category, null, lines, treatmentIsRequired, evaluationLabels.treatment)
        }
    }

    private fun hasHadSomeTreatmentsOfCategoryWithTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val lines = function.param<IntegerParameter>(2).value
            HasHadSomeTreatmentsWithCategoryOfTypes(category, types, lines, evaluationLabels.treatment)
        }
    }

    private fun hasHadLimitedTreatmentsOfCategoryWithTypesCreator(treatmentIsRequired: Boolean): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val lines = function.param<IntegerParameter>(2).value
            HasHadLimitedTreatmentsWithCategoryOfTypes(category, types, lines, treatmentIsRequired, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentsOfCategoryWithTypesAndStopReasonNotPDCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPD(category, types, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val weeks = function.param<IntegerParameter>(2).value
            HasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPD(category, types, weeks, evaluationLabels.treatment)
        }
    }

    private fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val weeks = function.param<IntegerParameter>(2).value
            HasHadLimitedWeeksOfTreatmentOfCategoryWithTypes(category, types, weeks, evaluationLabels.treatment)
        }
    }

    private fun hasHadSufficientWeeksOfTreatmentOfCategoryWithTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val weeks = function.param<IntegerParameter>(2).value
            HasHadSufficientWeeksOfTreatmentOfCategoryWithTypes(category, types, weeks, evaluationLabels.treatment)
        }
    }

    private fun hasHadAdjuvantTreatmentWithCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentCategoryOrTypeParameter>(0).value
            treatment.type?.let { mappedType ->
                HasHadAdjuvantTreatmentWithCategoryOfTypes(setOf(mappedType), treatment.category, evaluationLabels.treatment)
            } ?: HasHadAdjuvantTreatmentWithCategory(treatment.category, null, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadAdjuvantTreatmentWithCategoryWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY_OR_TYPE, Parameter.Type.INTEGER)
            val treatment = function.param<TreatmentCategoryOrTypeParameter>(0).value
            val weeksAgo = function.param<IntegerParameter>(1).value
            val minDate = referenceDate.minusWeeks(weeksAgo.toLong())
            HasHadAdjuvantTreatmentWithCategory(treatment.category, minDate, weeksAgo, evaluationLabels.treatment)
        }
    }

    private fun hasHadSystemicTherapyWithIntentsWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_INTENTS, Parameter.Type.INTEGER)
            val intents = function.param<ManyIntentsParameter>(0).value
            val weeks = function.param<IntegerParameter>(1).value
            val refDate = referenceDate.minusWeeks(weeks.toLong())
            HasHadSystemicTherapyWithAnyIntent(intents, refDate, weeks, true, evaluationLabels.treatment)
        }
    }

    private fun hasHadSystemicTherapyWithIntentsAtLeastWeeksAgoCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_INTENTS, Parameter.Type.INTEGER)
            val intents = function.param<ManyIntentsParameter>(0).value
            val weeks = function.param<IntegerParameter>(1).value
            val refDate = referenceDate.minusWeeks(weeks.toLong())
            HasHadSystemicTherapyWithAnyIntent(intents, refDate, weeks, false, evaluationLabels.treatment)
        }
    }

    private fun hasHadSystemicTreatmentInMetastaticSettingExcludingCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = function.param<TreatmentCategoryOrTypeParameter>(0).value
            HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSetting(
                referenceDate = referenceDate,
                intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
                categoryToIgnore = input.category,
                settingDescription = evaluationLabels.treatment.descriptionMetastatic(),
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadSystemicTherapyWithIntentsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val intents = function.param<ManyIntentsParameter>(0).value
            HasHadSystemicTherapyWithAnyIntent(intents, null, null, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadLimitedSystemicTreatmentsInTheMetastaticSettingCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
                referenceDate = referenceDate,
                intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
                settingDescription = evaluationLabels.treatment.descriptionMetastatic(),
                maximumLines = function.param<IntegerParameter>(0).value,
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadLimitedSystemicTreatmentsInTheAdvancedOrMetastaticSettingCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
                referenceDate = referenceDate,
                intentsToIgnore = setOf(Intent.CURATIVE),
                settingDescription = evaluationLabels.treatment.descriptionAdvancedOrMetastatic(),
                maximumLines = function.param<IntegerParameter>(0).value,
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadResponseFollowingTreatmentOfCategoryAndTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_RESPONSE,
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES
            )
            val treatmentResponse = function.param<TreatmentResponseParameter>(0).value
            val category = function.param<TreatmentCategoryParameter>(1).value
            val types = function.param<ManyTreatmentTypesParameter>(2).value
            HasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypes(
                treatmentResponses = setOf(treatmentResponse),
                category = category,
                types = types,
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadResponseFollowingSomeSpecificTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_RESPONSE, Parameter.Type.MANY_TREATMENTS)
            val treatmentResponse = function.param<TreatmentResponseParameter>(0).value
            val treatments = function.param<ManyTreatmentsParameter>(1).value
            HasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypes(
                treatmentResponses = setOf(treatmentResponse),
                targetTreatments = treatments,
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadRadiologicalResponseFollowingDrugTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.DRUG)
            val drug = function.param<DrugParameter>(0).value
            HasHadRadiologicalResponseFollowingDrugTreatment(drug = drug, labels = evaluationLabels.treatment)
        }
    }

    private fun hasHadClinicalBenefitFollowingSomeTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = function.param<ManyTreatmentsParameter>(0).value
            HasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypes(
                treatmentResponses = TreatmentResponse.BENEFIT_RESPONSES,
                targetTreatments = input,
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadClinicalBenefitFollowingTreatmentOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = function.param<TreatmentCategoryOrTypeParameter>(0).value
            HasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypes(
                treatmentResponses = TreatmentResponse.BENEFIT_RESPONSES,
                category = input.category,
                types = input.type?.let { setOf(it) },
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadClinicalBenefitFollowingTreatmentOfCategoryAndTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypes(
                treatmentResponses = TreatmentResponse.BENEFIT_RESPONSES,
                category = category,
                types = types,
                labels = evaluationLabels.treatment
            )
        }
    }

    private fun hasHadSystemicTreatmentOnlyOfCategoryOfTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadSystemicTreatmentOnlyOfCategoryOfTypes(category = category, types = types, labels = evaluationLabels.treatment)
        }
    }

    private fun hasHadSocTargetedTherapyForNsclcCreator(): FunctionCreator {
        return { HasHadSOCTargetedTherapyForNSCLC(emptySet(), evaluationLabels.treatment) }
    }

    private fun hasHadSocTargetedTherapyForNsclcExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            HasHadSOCTargetedTherapyForNSCLC(genes, evaluationLabels.treatment)
        }
    }

    private fun hasHadTargetedTherapyInterferingWithRasMekMapkPathwayCreator(): FunctionCreator {
        return { HasHadTargetedTherapyInterferingWithRasMekMapkPathway(evaluationLabels.treatment) }
    }

    private fun hasHadRadiotherapyToSomeBodyLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasHadRadiotherapyToSomeBodyLocation(function.param<StringParameter>(0).value, null, evaluationLabels.treatment)
        }
    }

    private fun hasHadRadiotherapyToSomeBodyLocationAndSufficientLinesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val bodyLocation = function.param<StringParameter>(0).value
            val lines = function.param<IntegerParameter>(1).value
            HasHadRadiotherapyToSomeBodyLocation(bodyLocation, lines, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryOfTypesAsMostRecentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(category, types, evaluationLabels.treatment)
        }
    }

    private fun hasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val months = function.param<IntegerParameter>(2).value
            HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecent(category, types, months, evaluationLabels.treatment)
        }
    }

    private fun hasHadChemoradiotherapyWithAnyDrugAndMinimumCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_DRUGS, Parameter.Type.INTEGER)
            val drugs = function.param<ManyDrugsParameter>(0).value
            val cycles = function.param<IntegerParameter>(1).value
            HasHadChemoradiotherapyWithDrugAndCycles(drugs, cycles, evaluationLabels.treatment)
        }
    }

    private fun hasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_TYPE, Parameter.Type.INTEGER)
            val chemotherapyType = function.param<TreatmentTypeParameter>(0).value
            val minCycles = function.param<IntegerParameter>(1).value
            HasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(chemotherapyType, minCycles, evaluationLabels.treatment)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentParameter>(0).value
            HasHadPDFollowingSpecificTreatment(listOf(treatment), evaluationLabels.treatment)
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val treatment = function.param<TreatmentCategoryOrTypeParameter>(0).value
            treatment.type?.let { mappedType ->
                HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(treatment.category, setOf(mappedType), null, null, evaluationLabels.treatment)
            } ?: HasHadPDFollowingTreatmentWithCategory(treatment.category, evaluationLabels.treatment)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.TREATMENT_CATEGORY, Parameter.Type.MANY_TREATMENT_TYPES)
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(category, types, null, null, evaluationLabels.treatment)
        }
    }

    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCyclesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val cycles = function.param<IntegerParameter>(2).value
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(category, types, cycles, null, evaluationLabels.treatment)
        }
    }

    //TODO: Check implementation
    private fun hasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val category = function.param<TreatmentCategoryParameter>(0).value
            val types = function.param<ManyTreatmentTypesParameter>(1).value
            val weeks = function.param<IntegerParameter>(2).value
            HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(category, types, null, weeks, evaluationLabels.treatment)
        }
    }

    private fun hasProgressiveDiseaseFollowingSomeSystemicTreatmentsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minSystemicTreatments = function.param<IntegerParameter>(0).value
            And(
                listOf(
                    HasHadSomeSystemicTreatments(minSystemicTreatments, evaluationLabels.treatment),
                    HasHadProgressionFollowingLatestTreatmentLine(mustBeRadiological = false, labels = evaluationLabels.treatment)
                )
            )
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentWithAnyDrugCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val drugs = function.param<ManyDrugsParameter>(0).value
            HasHadPDFollowingTreatmentWithAnyDrug(drugs, evaluationLabels.treatment)
        }
    }

    private fun hasProgressiveDiseaseFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.DRUG,
                Parameter.Type.TREATMENT_CATEGORY,
                Parameter.Type.MANY_TREATMENT_TYPES,
                Parameter.Type.INTEGER
            )
            val drug = function.param<DrugParameter>(0).value
            val category = function.param<TreatmentCategoryParameter>(1).value
            val types = function.param<ManyTreatmentTypesParameter>(2).value
            val weeks = function.param<IntegerParameter>(3).value
            HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(drug, category, types, weeks, evaluationLabels.treatment)
        }
    }

    private fun hasAcquiredResistanceToSomeDrugCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val drugs = function.param<ManyDrugsParameter>(0).value
            HasAcquiredResistanceToAnyDrug(drugs, evaluationLabels.treatment)
        }
    }

    //TODO: Check implementation
    private fun hasRadiologicalProgressionFollowingSomeTreatmentLinesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minSystemicTreatments = function.param<IntegerParameter>(0).value
            And(
                listOf(
                    HasHadSomeSystemicTreatments(minSystemicTreatments, evaluationLabels.treatment),
                    HasHadProgressionFollowingLatestTreatmentLine(mustBeRadiological = true, labels = evaluationLabels.treatment)
                )
            )
        }
    }

    private fun hasHadResectionWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxWeeksAgo = function.param<IntegerParameter>(0).value
            val minDate = referenceDate.minusWeeks(maxWeeksAgo.toLong())
            HasHadRecentResection(minDate, evaluationLabels.treatment)
        }
    }

    private fun hasPreviouslyParticipatedInSpecificTrialCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasPreviouslyParticipatedInTrial(function.param<StringParameter>(0).value, evaluationLabels.treatment)
        }
    }

    private fun isPrimaryPlatinumRefractoryWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            IsPrimaryPlatinumRefractoryWithinMonths(function.param<IntegerParameter>(0).value, referenceDate, evaluationLabels.treatment)
        }
    }
}
