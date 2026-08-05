package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Not
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.trial.BodyLocationParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.ManyDoidTermsParameter
import com.hartwig.actin.datamodel.trial.ManyStringsParameter
import com.hartwig.actin.datamodel.trial.ManyTnmTParameter
import com.hartwig.actin.datamodel.trial.ManyTumorStagesParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.ReceptorTypeParameter
import com.hartwig.actin.datamodel.trial.StringParameter
import com.hartwig.actin.datamodel.trial.TumorTypeParameter
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput

class TumorRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_SOLID_PRIMARY_TUMOR to hasSolidPrimaryTumorCreator(),
            EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA to hasSolidPrimaryTumorCreatorIncludingLymphomaCreator(),
            EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_ANY_DOID_TERM_X to hasPrimaryTumorBelongsToDoidTermsCreator(),
            EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_ANY_DOID_TERM_X_WITH_SUB_LOCATION_Y to hasPrimaryTumorBelongsToDoidTermsWithSubLocationCreator(),
            EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X to hasCancerOfUnknownPrimaryCreator(),
            EligibilityRule.HAS_CANCER_WITH_NEUROENDOCRINE_COMPONENT to hasCancerWithNeuroendocrineComponentCreator(),
            EligibilityRule.HAS_CANCER_WITH_SMALL_CELL_COMPONENT to hasCancerWithSmallCellComponentCreator(),
            EligibilityRule.HAS_CANCER_WITH_LARGE_CELL_COMPONENT to hasCancerWithLargeCellComponentCreator(),
            EligibilityRule.HAS_LOW_GRADE_CANCER to hasLowGradeCancerCreator(),
            EligibilityRule.HAS_HIGH_GRADE_CANCER to hasHighGradeCancerCreator(),
            EligibilityRule.HAS_WELL_DIFFERENTIATED_TUMOR to hasWellDifferentiatedTumorCreator(),
            EligibilityRule.HAS_KNOWN_SCLC_TRANSFORMATION to hasKnownSclcTransformationCreator(),
            EligibilityRule.HAS_NON_SQUAMOUS_NSCLC to hasNonSquamousNsclcCreator(),
            EligibilityRule.HAS_TRIPLE_NEGATIVE_BREAST_CANCER to hasTripleNegativeBreastCancerCreator(),
            EligibilityRule.HAS_BREAST_CANCER_RECEPTOR_X_POSITIVE to hasBreastCancerWithPositiveReceptorOfTypeCreator(),
            EligibilityRule.HAS_OVARIAN_CANCER_WITH_MUCINOUS_COMPONENT to hasOvarianCancerWithMucinousComponentCreator(),
            EligibilityRule.HAS_OVARIAN_BORDERLINE_TUMOR to hasOvarianBorderlineTumorCreator(),
            EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA to hasSecondaryGlioblastomaCreator(),
            EligibilityRule.HAS_NON_MUSCLE_INVASIVE_BLADDER_CANCER to hasNonMuscleInvasiveBladderCancerCreator(),
            EligibilityRule.HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to hasCytologicalDocumentationOfTumorTypeCreator(),
            EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to hasHistologicalDocumentationOfTumorTypeCreator(),
            EligibilityRule.HAS_PATHOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to hasPathologicalDocumentationOfTumorTypeCreator(),
            EligibilityRule.HAS_ANY_STAGE_X to hasAnyTumorStageCreator(),
            EligibilityRule.HAS_TNM_T_SCORE_X to hasSpecificTnmTScoreCreator(),
            EligibilityRule.HAS_LOCALLY_ADVANCED_CANCER to hasLocallyAdvancedCancerCreator(),
            EligibilityRule.HAS_METASTATIC_CANCER to hasMetastaticCancerCreator(),
            EligibilityRule.HAS_OLIGOMETASTATIC_CANCER to hasOligometastaticCancerCreator(),
            EligibilityRule.HAS_UNRESECTABLE_CANCER to hasUnresectableCancerCreator(),
            EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER to hasUnresectableStageIIICancerCreator(),
            EligibilityRule.HAS_RECURRENT_CANCER to hasRecurrentCancerCreator(),
            EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_RECURRENT_CANCER to meetsSpecificCriteriaRegardingRecurrentCancerCreator(),
            EligibilityRule.HAS_INCURABLE_CANCER to hasIncurableCancerCreator(),
            EligibilityRule.HAS_PRIMARY_TUMOR_AT_UNFAVOURABLE_SITE to hasPrimaryTumorAtUnfavourableSiteCreator(),
            EligibilityRule.HAS_ANY_LESION to hasAnyLesionCreator(),
            EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_METASTASES to meetsSpecificCriteriaRegardingMetastasesCreator(),
            EligibilityRule.HAS_LIVER_METASTASES to hasLiverMetastasesCreator(),
            EligibilityRule.HAS_LIVER_METASTASES_ONLY to hasOnlyLiverMetastasesCreator(),
            EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_LIVER_METASTASES to meetsSpecificCriteriaRegardingLiverMetastasesCreator(),
            EligibilityRule.HAS_LIVER_AND_OR_LYMPH_NODE_AND_OR_LUNG_METASTASES_ONLY to hasOnlyLiverAndOrLymphNodeAndOrLungMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_CNS_METASTASES to hasKnownCnsMetastasesCreator(),
            EligibilityRule.HAS_ACTIVE_CNS_METASTASES to hasKnownActiveCnsMetastasesCreator(),
            EligibilityRule.HAS_SYMPTOMATIC_CNS_METASTASES to hasKnownSymptomaticCnsMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_BRAIN_METASTASES to hasKnownBrainMetastasesCreator(),
            EligibilityRule.HAS_ACTIVE_BRAIN_METASTASES to hasKnownActiveBrainMetastasesCreator(),
            EligibilityRule.HAS_SYMPTOMATIC_BRAIN_METASTASES to hasKnownSymptomaticBrainMetastasesCreator(),
            EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_BRAIN_METASTASES to meetsSpecificCriteriaRegardingBrainMetastasesCreator(),
            EligibilityRule.HAS_EXTRACRANIAL_METASTASES to hasExtracranialMetastasesCreator(),
            EligibilityRule.HAS_BONE_METASTASES to hasBoneMetastasesCreator(),
            EligibilityRule.HAS_BONE_METASTASES_ONLY to hasOnlyBoneMetastasesCreator(),
            EligibilityRule.HAS_LUNG_METASTASES to hasLungMetastasesCreator(),
            EligibilityRule.HAS_LYMPH_NODE_METASTASES to hasLymphNodeMetastasesCreator(),
            EligibilityRule.HAS_LUNG_AND_OR_LUNG_LYMPH_NODE_METASTASES_ONLY to hasOnlyLungAndOrLungLymphNodeMetastasesCreator(),
            EligibilityRule.HAS_VISCERAL_METASTASES to hasVisceralMetastasesCreator(),
            EligibilityRule.HAS_IN_TRANSIT_METASTASES to hasInTransitMetastasesCreator(),
            EligibilityRule.HAS_SPLEEN_METASTASES to hasSpleenMetastasesCreator(),
            EligibilityRule.HAS_SOFT_TISSUE_METASTASES to hasSoftTissueMetastasesCreator(),
            EligibilityRule.HAS_UNRESECTABLE_PERITONEAL_METASTASES to hasUnresectablePeritonealMetastasesCreator(),
            EligibilityRule.HAS_LESIONS_CLOSE_TO_OR_INVOLVING_AIRWAY to hasLesionsCloseToOrInvolvingAirwayCreator(),
            EligibilityRule.HAS_LESIONS_INFILTRATING_BLOOD_VESSEL to { HasLesionsInfiltratingBloodVessel(evaluationLabels.tumor) },
            EligibilityRule.HAS_LESION_COUNT_OF_AT_LEAST_X_IN_BODY_LOCATION_Y to hasMinimumLesionsInSpecificBodyLocationCreator(),
            EligibilityRule.HAS_EXTENSIVE_SYSTEMIC_METASTASES_PREDOMINANTLY_DETERMINING_PROGNOSIS to hasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisCreator(),
            EligibilityRule.HAS_EXTENSIVE_ABDOMINAL_TUMOR_SPREAD to hasExtensiveAbdominalTumorSpreadCreator(),
            EligibilityRule.HAS_BIOPSY_AMENABLE_LESION to hasBiopsyAmenableLesionCreator(),
            EligibilityRule.HAS_IRRADIATION_AMENABLE_LESION to hasIrradiationAmenableLesionCreator(),
            EligibilityRule.HAS_HIFU_AMENABLE_LESION to hasHifuAmenableLesionCreator(),
            EligibilityRule.HAS_PRESENCE_OF_LESIONS_IN_AT_LEAST_X_SITES to hasMinimumSitesWithLesionsCreator(),
            EligibilityRule.HAS_RISK_OF_AT_LEAST_X_PERCENTAGE_FOR_SENTINEL_NODE_POSITIVITY to hasMinimumRiskForSentinelNodePositivityCreator(),
            EligibilityRule.HAS_OLIGOPROGRESSIVE_DISEASE to { HasOligoprogressiveDisease(evaluationLabels.tumor) },
            EligibilityRule.CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to canProvideFreshSampleForFurtherAnalysisCreator(),
            EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to canProvideSampleForFurtherAnalysisCreator(),
            EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY to meetsSpecificBiopsyRequirementsCreator(),
            EligibilityRule.HAS_VISIBLE_LESION_BY_CYSTOSCOPY to hasVisibleLesionByCystoscopyCreator(),
            EligibilityRule.HAS_EVALUABLE_DISEASE to hasEvaluableDiseaseCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE to hasMeasurableDiseaseCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST to hasMeasurableDiseaseRecistCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE_RANO to hasMeasurableDiseaseRanoCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE_PERCIST to hasMeasurableDiseasePercistCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA to hasSpecificProgressiveDiseaseCriteriaCreator(),
            EligibilityRule.HAS_RAPID_PROGRESSIVE_DISEASE to hasRapidProgressiveDiseaseCreator(),
            EligibilityRule.HAS_INJECTION_AMENABLE_LESION to hasInjectionAmenableLesionCreator(),
            EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION to hasMRIVolumeAmenableLesionCreator(),
            EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI to hasEvidenceOfCNSHemorrhageByMRICreator(),
            EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI to hasIntratumoralHemorrhageByMRICreator(),
            EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT to hasLowRiskOfHemorrhageUponTreatmentCreator(),
            EligibilityRule.HAS_SUPERSCAN_BONE_SCAN to hasSuperScanBoneScanCreator(),
            EligibilityRule.HAS_BCLC_STAGE_X to hasBCLCStageCreator(),
            EligibilityRule.HAS_SIEWERT_TYPE_X to hasSiewertTypeCreator(),
            EligibilityRule.HAS_ANY_RISK_X_CANCER to hasAnyRiskCancerCreator(),
            EligibilityRule.HAS_MODIFIED_OBERLIN_PROGNOSTIC_SCORE_OF_AT_LEAST_X to hasMinimumModifiedOberlinPrognosticScoreCreator(),
            EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR to hasLeftSidedColorectalTumorCreator(),
            EligibilityRule.HAS_SYMPTOMS_OF_PRIMARY_TUMOR_IN_SITU to hasSymptomsOfPrimaryTumorInSituCreator(),
            EligibilityRule.HAS_TUMOR_LENGTH_OF_AT_MOST_X_CM to hasLimitedTumorLengthCreator(),
        )
    }

    private fun hasSolidPrimaryTumorCreator(): FunctionCreator {
        return { HasSolidPrimaryTumor(doidModel, evaluationLabels.tumor) }
    }

    private fun hasSolidPrimaryTumorCreatorIncludingLymphomaCreator(): FunctionCreator {
        return { HasSolidPrimaryTumorIncludingLymphoma(doidModel, evaluationLabels.tumor) }
    }

    private fun hasPrimaryTumorBelongsToDoidTermsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val doidInputToMatch = function.param<ManyDoidTermsParameter>(0).value
            val doidsToMatch = doidInputToMatch.map { doidModel.toDoid(it) }.toSet()
            PrimaryTumorLocationBelongsToDoid(doidModel, cuppaToDoidMapping, doidsToMatch, null, evaluationLabels.tumor)
        }
    }

    private fun hasPrimaryTumorBelongsToDoidTermsWithSubLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val doidInputToMatch = function.param<ManyDoidTermsParameter>(0).value
            val doidsToMatch = doidInputToMatch.map { doidModel.toDoid(it) }.toSet()
            val subLocation = function.param<StringParameter>(1).value
            PrimaryTumorLocationBelongsToDoid(doidModel, cuppaToDoidMapping, doidsToMatch, subLocation, evaluationLabels.tumor)
        }
    }

    private fun hasCancerOfUnknownPrimaryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val tumorType = function.param<TumorTypeParameter>(0).value
            HasCancerOfUnknownPrimary(doidModel, TumorTypeInput.fromString(tumorType), evaluationLabels.tumor)
        }
    }

    private fun hasBreastCancerWithPositiveReceptorOfTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val receptorType = function.param<ReceptorTypeParameter>(0).value
            HasBreastCancerWithPositiveReceptorOfType(doidModel, receptorType, evaluationLabels.molecular, evaluationLabels.tumor)
        }
    }

    private fun hasCancerWithNeuroendocrineComponentCreator(): FunctionCreator {
        return { HasCancerWithNeuroendocrineComponent(doidModel, evaluationLabels.molecular, evaluationLabels.tumor) }
    }

    private fun hasCancerWithSmallCellComponentCreator(): FunctionCreator {
        return { HasCancerWithSmallCellComponent(doidModel, evaluationLabels.tumor) }
    }

    private fun hasCancerWithLargeCellComponentCreator(): FunctionCreator {
        return { HasCancerWithLargeCellComponent(doidModel, evaluationLabels.tumor) }
    }

    private fun hasLowGradeCancerCreator(): FunctionCreator {
        return { HasLowGradeCancer(evaluationLabels.tumor) }
    }

    private fun hasHighGradeCancerCreator(): FunctionCreator {
        return { Not(HasLowGradeCancer(evaluationLabels.tumor)) }
    }

    private fun hasWellDifferentiatedTumorCreator(): FunctionCreator {
        return { HasWellDifferentiatedTumor(evaluationLabels.tumor) }
    }

    private fun hasKnownSclcTransformationCreator(): FunctionCreator {
        return { HasKnownSclcTransformation(doidModel, evaluationLabels.molecular, evaluationLabels.tumor) }
    }

    private fun hasNonSquamousNsclcCreator(): FunctionCreator {
        return { HasNonSquamousNsclc(doidModel, evaluationLabels.tumor) }
    }

    private fun hasTripleNegativeBreastCancerCreator(): FunctionCreator {
        return { HasTripleNegativeBreastCancer(doidModel, evaluationLabels.molecular, evaluationLabels.tumor) }
    }

    private fun hasOvarianCancerWithMucinousComponentCreator(): FunctionCreator {
        return { HasOvarianCancerWithMucinousComponent(doidModel, evaluationLabels.tumor) }
    }

    private fun hasOvarianBorderlineTumorCreator(): FunctionCreator {
        return { HasOvarianBorderlineTumor(doidModel, evaluationLabels.tumor) }
    }

    private fun hasSecondaryGlioblastomaCreator(): FunctionCreator {
        return { HasSecondaryGlioblastoma(doidModel, evaluationLabels.tumor) }
    }

    private fun hasNonMuscleInvasiveBladderCancerCreator(): FunctionCreator {
        return { HasNonMuscleInvasiveBladderCancer(doidModel, evaluationLabels.tumor) }
    }

    private fun hasCytologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return { HasDocumentationOfTumorType(evaluationLabels.tumor.descriptionCytological(), evaluationLabels.tumor) }
    }

    private fun hasHistologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return { HasDocumentationOfTumorType(evaluationLabels.tumor.descriptionHistological(), evaluationLabels.tumor) }
    }

    private fun hasPathologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return { HasDocumentationOfTumorType(evaluationLabels.tumor.descriptionPathological(), evaluationLabels.tumor) }
    }

    private fun hasAnyTumorStageCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val stagesToMatch = function.param<ManyTumorStagesParameter>(0).value
            DerivedTumorStageEvaluationFunction(
                HasTumorStage(stagesToMatch, evaluationLabels.tumor),
                evaluationLabels.tumor.descriptionTumorStages(Format.concatItemsWithOr(stagesToMatch)),
                evaluationLabels.tumor
            )
        }
    }

    private fun hasSpecificTnmTScoreCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val scores = function.param<ManyTnmTParameter>(0).value
            HasTnmTScore(scores, evaluationLabels.tumor)
        }
    }

    private fun hasLocallyAdvancedCancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(
                HasLocallyAdvancedCancer(evaluationLabels.tumor), evaluationLabels.tumor.descriptionLocallyAdvancedCancer(), evaluationLabels.tumor
            )
        }
    }

    private fun hasMetastaticCancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(
                HasMetastaticCancer(doidModel, evaluationLabels.tumor), evaluationLabels.tumor.descriptionMetastaticCancer(), evaluationLabels.tumor
            )
        }
    }

    private fun hasOligometastaticCancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(
                HasOligometastaticCancer(doidModel, evaluationLabels.tumor),
                evaluationLabels.tumor.descriptionOligometastaticCancer(),
                evaluationLabels.tumor
            )
        }
    }

    private fun hasUnresectableCancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(
                HasUnresectableCancer(evaluationLabels.tumor), evaluationLabels.tumor.descriptionUnresectableCancer(), evaluationLabels.tumor
            )
        }
    }

    private fun hasUnresectablePeritonealMetastasesCreator(): FunctionCreator {
        return { HasUnresectablePeritonealMetastases(evaluationLabels.tumor) }
    }

    private fun hasLesionsCloseToOrInvolvingAirwayCreator(): FunctionCreator {
        return { HasLesionsCloseToOrInvolvingAirway(doidModel, evaluationLabels.tumor) }
    }

    private fun hasMinimumLesionsInSpecificBodyLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.INTEGER, Parameter.Type.BODY_LOCATION)
            val minLesions = function.param<IntegerParameter>(0).value
            val bodyLocation = function.param<BodyLocationParameter>(1).value
            HasMinimumLesionsInSpecificBodyLocation(minLesions, bodyLocation, evaluationLabels.tumor)
        }
    }

    private fun hasUnresectableStageIIICancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(
                HasUnresectableStageIIICancer(evaluationLabels.tumor),
                evaluationLabels.tumor.descriptionUnresectableStageIiiCancer(),
                evaluationLabels.tumor
            )
        }
    }

    private fun hasRecurrentCancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(
                HasRecurrentCancer(evaluationLabels.tumor), evaluationLabels.tumor.descriptionRecurrentCancer(), evaluationLabels.tumor
            )
        }
    }

    private fun meetsSpecificCriteriaRegardingRecurrentCancerCreator(): FunctionCreator {
        return { MeetsSpecificCriteriaRegardingRecurrentCancer(evaluationLabels.tumor) }
    }

    private fun hasIncurableCancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(
                HasIncurableCancer(evaluationLabels.tumor), evaluationLabels.tumor.descriptionIncurableCancer(), evaluationLabels.tumor
            )
        }
    }

    private fun hasPrimaryTumorAtUnfavourableSiteCreator(): FunctionCreator {
        return { HasPrimaryTumorAtUnfavourableSite(evaluationLabels.tumor) }
    }

    private fun hasAnyLesionCreator(): FunctionCreator {
        return { HasAnyLesion(evaluationLabels.tumor) }
    }

    private fun meetsSpecificCriteriaRegardingMetastasesCreator(): FunctionCreator {
        return { MeetsSpecificCriteriaRegardingMetastases(HasMetastaticCancer(doidModel, evaluationLabels.tumor), evaluationLabels.tumor) }
    }

    private fun hasLiverMetastasesCreator(): FunctionCreator {
        return { HasLiverMetastases(evaluationLabels.tumor) }
    }

    private fun hasOnlyLiverMetastasesCreator(): FunctionCreator {
        return {
            HasSpecificMetastasesOnly(
                listOf(TumorDetails::hasLiverLesions),
                listOf(TumorDetails::hasSuspectedLiverLesions),
                evaluationLabels.tumor.descriptionLiver(),
                evaluationLabels.tumor
            )
        }
    }

    private fun meetsSpecificCriteriaRegardingLiverMetastasesCreator(): FunctionCreator {
        return { MeetsSpecificCriteriaRegardingLiverMetastases(evaluationLabels.tumor) }
    }

    private fun hasOnlyLiverAndOrLymphNodeAndOrLungMetastasesCreator(): FunctionCreator {
        return {
            HasSpecificMetastasesOnly(
                listOf(
                    TumorDetails::hasLiverLesions,
                    TumorDetails::hasLymphNodeLesions,
                    TumorDetails::hasLungLesions
                ),
                listOf(
                    TumorDetails::hasSuspectedLiverLesions,
                    TumorDetails::hasSuspectedLymphNodeLesions,
                    TumorDetails::hasSuspectedLungLesions
                ),
                evaluationLabels.tumor.descriptionLiverAndOrLymphNodeAndOrLung(),
                evaluationLabels.tumor
            )
        }
    }

    private fun hasKnownCnsMetastasesCreator(): FunctionCreator {
        return { HasKnownCnsMetastases(evaluationLabels.tumor) }
    }

    private fun hasKnownActiveCnsMetastasesCreator(): FunctionCreator {
        return { HasKnownActiveCnsMetastases(evaluationLabels.tumor) }
    }

    private fun hasKnownSymptomaticCnsMetastasesCreator(): FunctionCreator {
        return { HasKnownSymptomaticCnsMetastases(evaluationLabels.tumor) }
    }

    private fun hasKnownBrainMetastasesCreator(): FunctionCreator {
        return { HasKnownBrainMetastases(evaluationLabels.tumor) }
    }

    private fun hasKnownActiveBrainMetastasesCreator(): FunctionCreator {
        return { HasKnownActiveBrainMetastases(evaluationLabels.tumor) }
    }

    private fun hasKnownSymptomaticBrainMetastasesCreator(): FunctionCreator {
        return { HasKnownSymptomaticBrainMetastases(evaluationLabels.tumor) }
    }

    private fun meetsSpecificCriteriaRegardingBrainMetastasesCreator(): FunctionCreator {
        return { MeetsSpecificCriteriaRegardingBrainMetastases(evaluationLabels.tumor) }
    }

    private fun hasExtracranialMetastasesCreator(): FunctionCreator {
        return { HasExtracranialMetastases(evaluationLabels.tumor) }
    }

    private fun hasBoneMetastasesCreator(): FunctionCreator {
        return { HasBoneMetastases(evaluationLabels.tumor) }
    }

    private fun hasOnlyBoneMetastasesCreator(): FunctionCreator {
        return {
            HasSpecificMetastasesOnly(
                listOf(TumorDetails::hasBoneLesions),
                listOf(TumorDetails::hasSuspectedBoneLesions),
                evaluationLabels.tumor.descriptionBone(),
                evaluationLabels.tumor
            )
        }
    }

    private fun hasLungMetastasesCreator(): FunctionCreator {
        return { HasLungMetastases(evaluationLabels.tumor) }
    }

    private fun hasLymphNodeMetastasesCreator(): FunctionCreator {
        return { HasLymphNodeMetastases(evaluationLabels.tumor) }
    }

    private fun hasOnlyLungAndOrLungLymphNodeMetastasesCreator(): FunctionCreator {
        return { HasOnlyLungAndOrLungLymphNodeMetastases(evaluationLabels.tumor) }
    }

    private fun hasVisceralMetastasesCreator(): FunctionCreator {
        return { HasVisceralMetastases(evaluationLabels.tumor) }
    }

    private fun hasInTransitMetastasesCreator(): FunctionCreator {
        return { HasInTransitMetastases(evaluationLabels.tumor) }
    }

    private fun hasSpleenMetastasesCreator(): FunctionCreator {
        return { HasSpleenMetastases(evaluationLabels.tumor) }
    }

    private fun hasSoftTissueMetastasesCreator(): FunctionCreator {
        return { HasSoftTissueMetastases(evaluationLabels.tumor) }
    }

    private fun hasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisCreator(): FunctionCreator {
        return {
            HasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosis(
                HasMetastaticCancer(doidModel, evaluationLabels.tumor), evaluationLabels.tumor
            )
        }
    }

    private fun hasExtensiveAbdominalTumorSpreadCreator(): FunctionCreator {
        return { HasExtensiveAbdominalTumorSpread(HasMetastaticCancer(doidModel, evaluationLabels.tumor), evaluationLabels.tumor) }
    }

    private fun hasBiopsyAmenableLesionCreator(): FunctionCreator {
        return { HasBiopsyAmenableLesion(evaluationLabels.tumor) }
    }

    private fun hasIrradiationAmenableLesionCreator(): FunctionCreator {
        return { HasIrradiationAmenableLesion(HasMetastaticCancer(doidModel, evaluationLabels.tumor), evaluationLabels.tumor) }
    }

    private fun hasHifuAmenableLesionCreator(): FunctionCreator {
        return { HasHifuAmenableLesion(evaluationLabels.tumor) }
    }

    private fun hasMinimumSitesWithLesionsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMinimumSitesWithLesions(function.param<IntegerParameter>(0).value, evaluationLabels.tumor)
        }
    }

    private fun hasMinimumRiskForSentinelNodePositivityCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMinimumRiskForSentinelNodePositivity(function.param<IntegerParameter>(0).value, evaluationLabels.tumor)
        }
    }

    private fun canProvideFreshSampleForFurtherAnalysisCreator(): FunctionCreator {
        return { CanProvideFreshSampleForFurtherAnalysis(evaluationLabels.tumor) }
    }

    private fun canProvideSampleForFurtherAnalysisCreator(): FunctionCreator {
        return { CanProvideSampleForFurtherAnalysis(evaluationLabels.tumor) }
    }

    private fun meetsSpecificBiopsyRequirementsCreator(): FunctionCreator {
        return { MeetsSpecificBiopsyRequirements(evaluationLabels.tumor) }
    }

    private fun hasVisibleLesionByCystoscopyCreator(): FunctionCreator {
        return { HasVisibleLesionByCystoscopy(evaluationLabels.tumor) }
    }

    private fun hasEvaluableDiseaseCreator(): FunctionCreator {
        return { HasEvaluableDisease(evaluationLabels.tumor) }
    }

    private fun hasMeasurableDiseaseCreator(): FunctionCreator {
        return { HasMeasurableDisease(evaluationLabels.tumor) }
    }

    private fun hasMeasurableDiseaseRecistCreator(): FunctionCreator {
        return { HasMeasurableDiseaseRecist(doidModel, evaluationLabels.tumor) }
    }

    private fun hasMeasurableDiseaseRanoCreator(): FunctionCreator {
        return { HasMeasurableDiseaseRano(doidModel, evaluationLabels.tumor) }
    }

    private fun hasMeasurableDiseasePercistCreator(): FunctionCreator {
        return { HasMeasurableDiseasePercist(doidModel, evaluationLabels.tumor) }
    }

    private fun hasSpecificProgressiveDiseaseCriteriaCreator(): FunctionCreator {
        return { HasSpecificProgressiveDiseaseCriteria(evaluationLabels.tumor) }
    }

    private fun hasRapidProgressiveDiseaseCreator(): FunctionCreator {
        return { HasRapidProgressiveDisease(evaluationLabels.tumor) }
    }

    private fun hasInjectionAmenableLesionCreator(): FunctionCreator {
        return { HasInjectionAmenableLesion(evaluationLabels.tumor) }
    }

    private fun hasMRIVolumeAmenableLesionCreator(): FunctionCreator {
        return { HasMRIVolumeAmenableLesion(evaluationLabels.tumor) }
    }

    private fun hasEvidenceOfCNSHemorrhageByMRICreator(): FunctionCreator {
        return { HasEvidenceOfCNSHemorrhageByMRI(evaluationLabels.tumor) }
    }

    private fun hasIntratumoralHemorrhageByMRICreator(): FunctionCreator {
        return { HasIntratumoralHemorrhageByMRI(evaluationLabels.tumor) }
    }

    private fun hasLowRiskOfHemorrhageUponTreatmentCreator(): FunctionCreator {
        return { HasLowRiskOfHemorrhageUponTreatment(evaluationLabels.tumor) }
    }

    private fun hasSuperScanBoneScanCreator(): FunctionCreator {
        return { HasSuperScanBoneScan(evaluationLabels.tumor) }
    }

    private fun hasBCLCStageCreator(): FunctionCreator {
        return { HasBCLCStage(evaluationLabels.tumor) }
    }

    private fun hasSiewertTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasSiewertType(function.param<StringParameter>(0).value, evaluationLabels.tumor)
        }
    }

    private fun hasAnyRiskCancerCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasAnyRiskCancer(function.param<ManyStringsParameter>(0).value, evaluationLabels.tumor)
        }
    }

    private fun hasMinimumModifiedOberlinPrognosticScoreCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMinimumModifiedOberlinPrognosticScore(function.param<IntegerParameter>(0).value, evaluationLabels.tumor)
        }
    }

    private fun hasLeftSidedColorectalTumorCreator(): FunctionCreator {
        return { HasLeftSidedColorectalTumor(doidModel, evaluationLabels.tumor) }
    }

    private fun hasSymptomsOfPrimaryTumorInSituCreator(): FunctionCreator {
        return { HasSymptomsOfPrimaryTumorInSitu(evaluationLabels.tumor) }
    }

    private fun hasLimitedTumorLengthCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasLimitedTumorLength(function.param<IntegerParameter>(0).value, evaluationLabels.tumor)
        }
    }
}
