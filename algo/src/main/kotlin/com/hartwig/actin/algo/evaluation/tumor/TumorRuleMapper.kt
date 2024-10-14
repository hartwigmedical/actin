package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class TumorRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_SOLID_PRIMARY_TUMOR to hasSolidPrimaryTumorCreator(),
            EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA to hasSolidPrimaryTumorCreatorIncludingLymphomaCreator(),
            EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_ANY_DOID_TERM_X to hasPrimaryTumorBelongsToDoidTermsCreator(),
            EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_ANY_DOID_TERM_X_DISTAL_SUB_LOCATION to hasPrimaryTumorBelongsToDoidTermsDistalSubLocationCreator(),
            EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X to hasCancerOfUnknownPrimaryCreator(),
            EligibilityRule.HAS_CANCER_WITH_NEUROENDOCRINE_COMPONENT to hasCancerWithNeuroendocrineComponentCreator(),
            EligibilityRule.HAS_CANCER_WITH_SMALL_CELL_COMPONENT to hasCancerWithSmallCellComponentCreator(),
            EligibilityRule.HAS_KNOWN_SCLC_TRANSFORMATION to hasKnownSCLCTransformationCreator(),
            EligibilityRule.HAS_NON_SQUAMOUS_NSCLC to hasNonSquamousNSCLCCreator(),
            EligibilityRule.HAS_BREAST_CANCER_RECEPTOR_X_POSITIVE to hasBreastCancerWithPositiveReceptorOfTypeCreator(),
            EligibilityRule.HAS_OVARIAN_CANCER_WITH_MUCINOUS_COMPONENT to hasOvarianCancerWithMucinousComponentCreator(),
            EligibilityRule.HAS_OVARIAN_BORDERLINE_TUMOR to hasOvarianBorderlineTumorCreator(),
            EligibilityRule.HAS_STOMACH_UNDIFFERENTIATED_TUMOR to hasStomachUndifferentiatedTumorCreator(),
            EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA to hasSecondaryGlioblastomaCreator(),
            EligibilityRule.HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to hasCytologicalDocumentationOfTumorTypeCreator(),
            EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to hasHistologicalDocumentationOfTumorTypeCreator(),
            EligibilityRule.HAS_ANY_STAGE_X to hasAnyTumorStageCreator(),
            EligibilityRule.HAS_LOCALLY_ADVANCED_CANCER to hasLocallyAdvancedCancerCreator(),
            EligibilityRule.HAS_METASTATIC_CANCER to hasMetastaticCancerCreator(),
            EligibilityRule.HAS_UNRESECTABLE_CANCER to hasUnresectableCancerCreator(),
            EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER to hasUnresectableStageIIICancerCreator(),
            EligibilityRule.HAS_RECURRENT_CANCER to hasRecurrentCancerCreator(),
            EligibilityRule.HAS_INCURABLE_CANCER to hasIncurableCancerCreator(),
            EligibilityRule.HAS_ANY_LESION to hasAnyLesionCreator(),
            EligibilityRule.HAS_LIVER_METASTASES to hasLiverMetastasesCreator(),
            EligibilityRule.HAS_LIVER_METASTASES_ONLY to hasOnlyLiverMetastasesCreator(),
            EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_LIVER_METASTASES to meetsSpecificCriteriaRegardingLiverMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_CNS_METASTASES to hasKnownCnsMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES to hasKnownActiveCnsMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_BRAIN_METASTASES to hasKnownBrainMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES to hasKnownActiveBrainMetastasesCreator(),
            EligibilityRule.MEETS_SPECIFIC_CRITERIA_REGARDING_BRAIN_METASTASES to meetsSpecificCriteriaRegardingBrainMetastasesCreator(),
            EligibilityRule.HAS_EXTRACRANIAL_METASTASES to hasExtracranialMetastasesCreator(),
            EligibilityRule.HAS_BONE_METASTASES to hasBoneMetastasesCreator(),
            EligibilityRule.HAS_BONE_METASTASES_ONLY to hasOnlyBoneMetastasesCreator(),
            EligibilityRule.HAS_LUNG_METASTASES to hasLungMetastasesCreator(),
            EligibilityRule.HAS_LYMPH_NODE_METASTASES to hasLymphNodeMetastasesCreator(),
            EligibilityRule.HAS_VISCERAL_METASTASES to hasVisceralMetastasesCreator(),
            EligibilityRule.HAS_UNRESECTABLE_PERITONEAL_METASTASES to hasUnresectablePeritonealMetastasesCreator(),
            EligibilityRule.HAS_LESIONS_CLOSE_TO_OR_INVOLVING_AIRWAY to hasLesionsCloseToOrInvolvingAirwayCreator(),
            EligibilityRule.HAS_EXTENSIVE_SYSTEMIC_METASTASES_PREDOMINANTLY_DETERMINING_PROGNOSIS to hasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisCreator(),
            EligibilityRule.HAS_BIOPSY_AMENABLE_LESION to hasBiopsyAmenableLesionCreator(),
            EligibilityRule.HAS_IRRADIATION_AMENABLE_LESION to hasIrradiationAmenableLesionCreator(),
            EligibilityRule.HAS_PRESENCE_OF_LESIONS_IN_AT_LEAST_X_SITES to hasMinimumSitesWithLesionsCreator(),
            EligibilityRule.CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to canProvideFreshSampleForFurtherAnalysisCreator(),
            EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to canProvideSampleForFurtherAnalysisCreator(),
            EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY to meetsSpecificBiopsyRequirementsCreator(),
            EligibilityRule.HAS_EVALUABLE_DISEASE to hasEvaluableDiseaseCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE to hasMeasurableDiseaseCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST to hasMeasurableDiseaseRecistCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE_RANO to hasMeasurableDiseaseRanoCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA to hasSpecificProgressiveDiseaseCriteriaCreator(),
            EligibilityRule.HAS_INJECTION_AMENABLE_LESION to hasInjectionAmenableLesionCreator(),
            EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION to hasMRIVolumeAmenableLesionCreator(),
            EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI to hasEvidenceOfCNSHemorrhageByMRICreator(),
            EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI to hasIntratumoralHemorrhageByMRICreator(),
            EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT to hasLowRiskOfHemorrhageUponTreatmentCreator(),
            EligibilityRule.HAS_SUPERSCAN_BONE_SCAN to hasSuperScanBoneScanCreator(),
            EligibilityRule.HAS_BCLC_STAGE_X to hasBCLCStageCreator(),
            EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR to hasLeftSidedColorectalTumorCreator(),
            EligibilityRule.HAS_SYMPTOMS_OF_PRIMARY_TUMOR_IN_SITU to hasSymptomsOfPrimaryTumorInSitu(),
        )
    }

    private fun hasSolidPrimaryTumorCreator(): FunctionCreator {
        return { HasSolidPrimaryTumor(doidModel()) }
    }

    private fun hasSolidPrimaryTumorCreatorIncludingLymphomaCreator(): FunctionCreator {
        return { HasSolidPrimaryTumorIncludingLymphoma(doidModel()) }
    }

    private fun hasPrimaryTumorBelongsToDoidTermsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val doidTermToMatch = functionInputResolver().createManyDoidTermsInput(function)
            val doidTermResolved = doidTermToMatch.mapNotNull { doidModel().resolveDoidForTerm(it) }
            PrimaryTumorLocationBelongsToDoid(doidModel(), doidTermResolved, null)
        }
    }

    private fun hasPrimaryTumorBelongsToDoidTermsDistalSubLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val doidTermToMatch = functionInputResolver().createManyDoidTermsInput(function)
            val doidTermResolved = doidTermToMatch.mapNotNull { doidModel().resolveDoidForTerm(it) }
            PrimaryTumorLocationBelongsToDoid(doidModel(), doidTermResolved, "distal")
        }
    }

    private fun hasCancerOfUnknownPrimaryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val categoryOfCUP = functionInputResolver().createOneTumorTypeInput(function)
            HasCancerOfUnknownPrimary(doidModel(), categoryOfCUP)
        }
    }

    private fun hasBreastCancerWithPositiveReceptorOfTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val receptorType = functionInputResolver().createOneReceptorTypeInput(function)
            HasBreastCancerWithPositiveReceptorOfType(doidModel(), receptorType, maxMolecularTestAge())
        }
    }

    private fun hasCancerWithNeuroendocrineComponentCreator(): FunctionCreator {
        return { HasCancerWithNeuroendocrineComponent(doidModel(), maxMolecularTestAge()) }
    }

    private fun hasCancerWithSmallCellComponentCreator(): FunctionCreator {
        return { HasCancerWithSmallCellComponent(doidModel()) }
    }

    private fun hasKnownSCLCTransformationCreator(): FunctionCreator {
        return { HasKnownSCLCTransformation(doidModel(), maxMolecularTestAge()) }
    }

    private fun hasNonSquamousNSCLCCreator(): FunctionCreator {
        return { HasNonSquamousNSCLC(doidModel()) }
    }

    private fun hasOvarianCancerWithMucinousComponentCreator(): FunctionCreator {
        return { HasOvarianCancerWithMucinousComponent(doidModel()) }
    }

    private fun hasOvarianBorderlineTumorCreator(): FunctionCreator {
        return { HasOvarianBorderlineTumor(doidModel()) }
    }

    private fun hasStomachUndifferentiatedTumorCreator(): FunctionCreator {
        return { HasStomachUndifferentiatedTumor(doidModel()) }
    }

    private fun hasSecondaryGlioblastomaCreator(): FunctionCreator {
        return { HasSecondaryGlioblastoma(doidModel()) }
    }

    private fun hasCytologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return { HasCytologicalDocumentationOfTumorType() }
    }

    private fun hasHistologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return { HasHistologicalDocumentationOfTumorType() }
    }

    private fun hasAnyTumorStageCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val stagesToMatch = functionInputResolver().createManyTumorStagesInput(function)
            HasTumorStage(stagesToMatch)
        }
    }

    private fun hasLocallyAdvancedCancerCreator(): FunctionCreator {
        return { DerivedTumorStageEvaluationFunction(HasLocallyAdvancedCancer()) }
    }

    private fun hasMetastaticCancerCreator(): FunctionCreator {
        return {
            DerivedTumorStageEvaluationFunction(HasMetastaticCancer(doidModel()))
        }
    }

    private fun hasUnresectableCancerCreator(): FunctionCreator {
        return { DerivedTumorStageEvaluationFunction(HasUnresectableCancer()) }
    }

    private fun hasUnresectablePeritonealMetastasesCreator(): FunctionCreator {
        return { HasUnresectablePeritonealMetastases() }
    }

    private fun hasLesionsCloseToOrInvolvingAirwayCreator(): FunctionCreator {
        return { HasLesionsCloseToOrInvolvingAirway(doidModel()) }
    }

    private fun hasUnresectableStageIIICancerCreator(): FunctionCreator {
        return { DerivedTumorStageEvaluationFunction(HasUnresectableStageIIICancer()) }
    }

    private fun hasRecurrentCancerCreator(): FunctionCreator {
        return { DerivedTumorStageEvaluationFunction(HasRecurrentCancer()) }
    }

    private fun hasIncurableCancerCreator(): FunctionCreator {
        return { DerivedTumorStageEvaluationFunction(HasIncurableCancer()) }
    }

    private fun hasAnyLesionCreator(): FunctionCreator {
        return { HasAnyLesion() }
    }

    private fun hasLiverMetastasesCreator(): FunctionCreator {
        return { HasLiverMetastases() }
    }

    private fun hasOnlyLiverMetastasesCreator(): FunctionCreator {
        return { HasSpecificMetastasesOnly(TumorDetails::hasLiverLesions, "liver") }
    }

    private fun hasKnownCnsMetastasesCreator(): FunctionCreator {
        return { HasKnownCnsMetastases() }
    }

    private fun hasKnownActiveCnsMetastasesCreator(): FunctionCreator {
        return { HasKnownActiveCnsMetastases() }
    }

    private fun hasKnownBrainMetastasesCreator(): FunctionCreator {
        return { HasKnownBrainMetastases() }
    }

    private fun hasKnownActiveBrainMetastasesCreator(): FunctionCreator {
        return { HasKnownActiveBrainMetastases() }
    }

    private fun meetsSpecificCriteriaRegardingBrainMetastasesCreator(): FunctionCreator {
        return { MeetsSpecificCriteriaRegardingBrainMetastases() }
    }

    private fun hasExtracranialMetastasesCreator(): FunctionCreator {
        return { HasExtracranialMetastases() }
    }

    private fun hasBoneMetastasesCreator(): FunctionCreator {
        return { HasBoneMetastases() }
    }

    private fun hasOnlyBoneMetastasesCreator(): FunctionCreator {
        return { HasSpecificMetastasesOnly(TumorDetails::hasBoneLesions, "bone") }
    }

    private fun hasLungMetastasesCreator(): FunctionCreator {
        return { HasLungMetastases() }
    }

    private fun hasLymphNodeMetastasesCreator(): FunctionCreator {
        return { HasLymphNodeMetastases() }
    }

    private fun hasVisceralMetastasesCreator(): FunctionCreator {
        return { HasVisceralMetastases() }
    }

    private fun hasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisCreator(): FunctionCreator {
        return { HasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosis(HasMetastaticCancer(doidModel())) }
    }

    private fun hasBiopsyAmenableLesionCreator(): FunctionCreator {
        return { HasBiopsyAmenableLesion() }
    }

    private fun hasIrradiationAmenableLesionCreator(): FunctionCreator {
        return { HasIrradiationAmenableLesion(HasMetastaticCancer(doidModel())) }
    }

    private fun hasMinimumSitesWithLesionsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMinimumSitesWithLesions(functionInputResolver().createOneIntegerInput(function))
        }
    }

    private fun canProvideFreshSampleForFurtherAnalysisCreator(): FunctionCreator {
        return { CanProvideFreshSampleForFurtherAnalysis() }
    }

    private fun canProvideSampleForFurtherAnalysisCreator(): FunctionCreator {
        return { CanProvideSampleForFurtherAnalysis() }
    }

    private fun meetsSpecificBiopsyRequirementsCreator(): FunctionCreator {
        return { MeetsSpecificBiopsyRequirements() }
    }

    private fun hasEvaluableDiseaseCreator(): FunctionCreator {
        return { HasEvaluableDisease() }
    }

    private fun hasMeasurableDiseaseCreator(): FunctionCreator {
        return { HasMeasurableDisease() }
    }

    private fun hasMeasurableDiseaseRecistCreator(): FunctionCreator {
        return { HasMeasurableDiseaseRecist(doidModel()) }
    }

    private fun hasMeasurableDiseaseRanoCreator(): FunctionCreator {
        return { HasMeasurableDiseaseRano(doidModel()) }
    }

    private fun hasSpecificProgressiveDiseaseCriteriaCreator(): FunctionCreator {
        return { HasSpecificProgressiveDiseaseCriteria() }
    }

    private fun hasInjectionAmenableLesionCreator(): FunctionCreator {
        return { HasInjectionAmenableLesion() }
    }

    private fun hasMRIVolumeAmenableLesionCreator(): FunctionCreator {
        return { HasMRIVolumeAmenableLesion() }
    }

    private fun hasEvidenceOfCNSHemorrhageByMRICreator(): FunctionCreator {
        return { HasEvidenceOfCNSHemorrhageByMRI() }
    }

    private fun hasIntratumoralHemorrhageByMRICreator(): FunctionCreator {
        return { HasIntratumoralHemorrhageByMRI() }
    }

    private fun hasLowRiskOfHemorrhageUponTreatmentCreator(): FunctionCreator {
        return { HasLowRiskOfHemorrhageUponTreatment() }
    }

    private fun hasSuperScanBoneScanCreator(): FunctionCreator {
        return { HasSuperScanBoneScan() }
    }

    private fun hasBCLCStageCreator(): FunctionCreator {
        return { HasBCLCStage() }
    }

    private fun hasLeftSidedColorectalTumorCreator(): FunctionCreator {
        return { HasLeftSidedColorectalTumor(doidModel()) }
    }

    private fun meetsSpecificCriteriaRegardingLiverMetastasesCreator(): FunctionCreator {
        return { MeetsSpecificCriteriaRegardingLiverMetastases() }
    }

    private fun hasSymptomsOfPrimaryTumorInSitu(): FunctionCreator {
        return { HasSymptomsOfPrimaryTumorInSitu() }
    }
}