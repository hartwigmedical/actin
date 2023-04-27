package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class TumorRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_SOLID_PRIMARY_TUMOR to hasSolidPrimaryTumorCreator(),
            EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA to hasSolidPrimaryTumorCreatorIncludingLymphomaCreator(),
            EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X to hasPrimaryTumorBelongsToDoidTermCreator(),
            EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X to hasCancerOfUnknownPrimaryCreator(),
            EligibilityRule.HAS_CANCER_WITH_NEUROENDOCRINE_COMPONENT to hasCancerWithNeuroendocrineComponentCreator(),
            EligibilityRule.HAS_CANCER_WITH_SMALL_CELL_COMPONENT to hasCancerWithSmallCellComponentCreator(),
            EligibilityRule.HAS_BREAST_CANCER_HORMONE_POSITIVE_AND_HER2_NEGATIVE to hasBreastCancerHormonePositiveHER2NegativeCreator(),
            EligibilityRule.HAS_PROSTATE_CANCER_WITH_SMALL_CELL_COMPONENT to hasProstateCancerWithSmallCellComponentCreator(),
            EligibilityRule.HAS_OVARIAN_CANCER_WITH_MUCINOUS_COMPONENT to hasOvarianCancerWithMucinousComponentCreator(),
            EligibilityRule.HAS_OVARIAN_BORDERLINE_TUMOR to hasOvarianBorderlineTumorCreator(),
            EligibilityRule.HAS_STOMACH_UNDIFFERENTIATED_TUMOR to hasStomachUndifferentiatedTumorCreator(),
            EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA to hasSecondaryGlioblastomaCreator(),
            EligibilityRule.HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to hasCytologicalDocumentationOfTumorTypeCreator(),
            EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE to hasHistologicalDocumentationOfTumorTypeCreator(),
            EligibilityRule.HAS_STAGE_X to hasTumorStageCreator(),
            EligibilityRule.HAS_LOCALLY_ADVANCED_CANCER to hasLocallyAdvancedCancerCreator(),
            EligibilityRule.HAS_METASTATIC_CANCER to hasMetastaticCancerCreator(),
            EligibilityRule.HAS_UNRESECTABLE_CANCER to hasUnresectableCancerCreator(),
            EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER to hasUnresectableStageIIICancerCreator(),
            EligibilityRule.HAS_RECURRENT_CANCER to hasRecurrentCancerCreator(),
            EligibilityRule.HAS_INCURABLE_CANCER to hasIncurableCancerCreator(),
            EligibilityRule.HAS_ANY_LESION to hasAnyLesionCreator(),
            EligibilityRule.HAS_LIVER_METASTASES to hasLivesMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_CNS_METASTASES to hasKnownCnsMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES to hasKnownActiveCnsMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_BRAIN_METASTASES to hasKnownBrainMetastasesCreator(),
            EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES to hasKnownActiveBrainMetastasesCreator(),
            EligibilityRule.HAS_EXTRACRANIAL_METASTASES to hasExtracranialMetastasesCreator(),
            EligibilityRule.HAS_BONE_METASTASES to hasBoneMetastasesCreator(),
            EligibilityRule.HAS_BONE_METASTASES_ONLY to hasOnlyBoneMetastasesCreator(),
            EligibilityRule.HAS_LUNG_METASTASES to hasLungMetastasesCreator(),
            EligibilityRule.HAS_LYMPH_NODE_METASTASES to hasLymphNodeMetastasesCreator(),
            EligibilityRule.HAS_VISCERAL_METASTASES to hasVisceralMetastasesCreator(),
            EligibilityRule.HAS_BIOPSY_AMENABLE_LESION to hasBiopsyAmenableLesionCreator(),
            EligibilityRule.HAS_PRESENCE_OF_LESIONS_IN_AT_LEAST_X_SITES to hasMinimumSitesWithLesionsCreator(),
            EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC to tumorBiopsyTakenBeforeInformedConsentCreator(),
            EligibilityRule.CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to canProvideFreshSampleForFurtherAnalysisCreator(),
            EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS to canProvideSampleForFurtherAnalysisCreator(),
            EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY to meetsSpecificBiopsyRequirementsCreator(),
            EligibilityRule.HAS_EVALUABLE_DISEASE to hasEvaluableDiseaseCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE to hasMeasurableDiseaseCreator(),
            EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST to hasMeasurableDiseaseRecistCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA to hasSpecificProgressiveDiseaseCriteriaCreator(),
            EligibilityRule.HAS_INJECTION_AMENABLE_LESION to hasInjectionAmenableLesionCreator(),
            EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION to hasMRIVolumeAmenableLesionCreator(),
            EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI to hasEvidenceOfCNSHemorrhageByMRICreator(),
            EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI to hasIntratumoralHemorrhageByMRICreator(),
            EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT to hasLowRiskOfHemorrhageUponTreatmentCreator(),
            EligibilityRule.HAS_SUPERSCAN_BONE_SCAN to hasSuperScanBoneScanCreator(),
            EligibilityRule.HAS_CHILD_PUGH_CLASS_X_LIVER_SCORE to hasChildPughClassCreator(),
            EligibilityRule.HAS_BCLC_STAGE_X to hasBCLCStageCreator(),
            EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR to hasLeftSidedColorectalTumorCreator(),
        )
    }

    private fun hasSolidPrimaryTumorCreator(): FunctionCreator {
        return FunctionCreator { HasSolidPrimaryTumor(doidModel()) }
    }

    private fun hasSolidPrimaryTumorCreatorIncludingLymphomaCreator(): FunctionCreator {
        return FunctionCreator { HasSolidPrimaryTumorIncludingLymphoma(doidModel()) }
    }

    private fun hasPrimaryTumorBelongsToDoidTermCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val doidTermToMatch = functionInputResolver().createOneDoidTermInput(function)
            PrimaryTumorLocationBelongsToDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToMatch)!!)
        }
    }

    private fun hasCancerOfUnknownPrimaryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val categoryOfCUP = functionInputResolver().createOneTumorTypeInput(function)
            HasCancerOfUnknownPrimary(doidModel(), categoryOfCUP)
        }
    }

    private fun hasBreastCancerHormonePositiveHER2NegativeCreator(): FunctionCreator {
        return FunctionCreator { HasBreastCancerHormonePositiveHER2Negative(doidModel()) }
    }

    private fun hasCancerWithNeuroendocrineComponentCreator(): FunctionCreator {
        return FunctionCreator { HasCancerWithNeuroendocrineComponent(doidModel()) }
    }

    private fun hasCancerWithSmallCellComponentCreator(): FunctionCreator {
        return FunctionCreator { HasCancerWithSmallCellComponent(doidModel()) }
    }

    private fun hasProstateCancerWithSmallCellComponentCreator(): FunctionCreator {
        return FunctionCreator { HasProstateCancerWithSmallCellComponent(doidModel()) }
    }

    private fun hasOvarianCancerWithMucinousComponentCreator(): FunctionCreator {
        return FunctionCreator { HasOvarianCancerWithMucinousComponent(doidModel()) }
    }

    private fun hasOvarianBorderlineTumorCreator(): FunctionCreator {
        return FunctionCreator { HasOvarianBorderlineTumor(doidModel()) }
    }

    private fun hasStomachUndifferentiatedTumorCreator(): FunctionCreator {
        return FunctionCreator { HasStomachUndifferentiatedTumor(doidModel()) }
    }

    private fun hasSecondaryGlioblastomaCreator(): FunctionCreator {
        return FunctionCreator { HasSecondaryGlioblastoma(doidModel()) }
    }

    private fun hasCytologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return FunctionCreator { HasCytologicalDocumentationOfTumorType() }
    }

    private fun hasHistologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return FunctionCreator { HasHistologicalDocumentationOfTumorType() }
    }

    private fun hasTumorStageCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val stageToMatch = functionInputResolver().createOneTumorStageInput(function)
            HasTumorStage(TumorStageDerivationFunction.create(doidModel()), stageToMatch)
        }
    }

    private fun hasLocallyAdvancedCancerCreator(): FunctionCreator {
        return FunctionCreator {
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()), HasLocallyAdvancedCancer())
        }
    }

    private fun hasMetastaticCancerCreator(): FunctionCreator {
        return FunctionCreator {
            DerivedTumorStageEvaluationFunction(
                TumorStageDerivationFunction.create(doidModel()), HasMetastaticCancer(doidModel())
            )
        }
    }

    private fun hasUnresectableCancerCreator(): FunctionCreator {
        return FunctionCreator {
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()), HasUnresectableCancer())
        }
    }

    private fun hasUnresectableStageIIICancerCreator(): FunctionCreator {
        return FunctionCreator {
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()), HasUnresectableStageIIICancer())
        }
    }

    private fun hasRecurrentCancerCreator(): FunctionCreator {
        return FunctionCreator {
            DerivedTumorStageEvaluationFunction(
                TumorStageDerivationFunction.create(doidModel()),
                HasRecurrentCancer()
            )
        }
    }

    private fun hasIncurableCancerCreator(): FunctionCreator {
        return FunctionCreator {
            DerivedTumorStageEvaluationFunction(
                TumorStageDerivationFunction.create(doidModel()),
                HasIncurableCancer()
            )
        }
    }

    private fun hasAnyLesionCreator(): FunctionCreator {
        return FunctionCreator { HasAnyLesion() }
    }

    private fun hasLivesMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasLiverMetastases() }
    }

    private fun hasKnownCnsMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasKnownCnsMetastases() }
    }

    private fun hasKnownActiveCnsMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasKnownActiveCnsMetastases() }
    }

    private fun hasKnownBrainMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasKnownBrainMetastases() }
    }

    private fun hasKnownActiveBrainMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasKnownActiveBrainMetastases() }
    }

    private fun hasExtracranialMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasExtracranialMetastases() }
    }

    private fun hasBoneMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasBoneMetastases() }
    }

    private fun hasOnlyBoneMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasBoneMetastasesOnly() }
    }

    private fun hasLungMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasLungMetastases() }
    }

    private fun hasLymphNodeMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasLymphNodeMetastases() }
    }

    private fun hasVisceralMetastasesCreator(): FunctionCreator {
        return FunctionCreator { HasVisceralMetastases() }
    }

    private fun hasBiopsyAmenableLesionCreator(): FunctionCreator {
        return FunctionCreator { HasBiopsyAmenableLesion() }
    }

    private fun hasMinimumSitesWithLesionsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            HasMinimumSitesWithLesions(functionInputResolver().createOneIntegerInput(function))
        }
    }

    private fun tumorBiopsyTakenBeforeInformedConsentCreator(): FunctionCreator {
        return FunctionCreator { TumorBiopsyTakenBeforeInformedConsent() }
    }

    private fun canProvideFreshSampleForFurtherAnalysisCreator(): FunctionCreator {
        return FunctionCreator { CanProvideFreshSampleForFurtherAnalysis() }
    }

    private fun canProvideSampleForFurtherAnalysisCreator(): FunctionCreator {
        return FunctionCreator { CanProvideSampleForFurtherAnalysis() }
    }

    private fun meetsSpecificBiopsyRequirementsCreator(): FunctionCreator {
        return FunctionCreator { MeetsSpecificBiopsyRequirements() }
    }

    private fun hasEvaluableDiseaseCreator(): FunctionCreator {
        return FunctionCreator { HasEvaluableDisease() }
    }

    private fun hasMeasurableDiseaseCreator(): FunctionCreator {
        return FunctionCreator { HasMeasurableDisease() }
    }

    private fun hasMeasurableDiseaseRecistCreator(): FunctionCreator {
        return FunctionCreator { HasMeasurableDiseaseRecist(doidModel()) }
    }

    private fun hasSpecificProgressiveDiseaseCriteriaCreator(): FunctionCreator {
        return FunctionCreator { HasSpecificProgressiveDiseaseCriteria() }
    }

    private fun hasInjectionAmenableLesionCreator(): FunctionCreator {
        return FunctionCreator { HasInjectionAmenableLesion() }
    }

    private fun hasMRIVolumeAmenableLesionCreator(): FunctionCreator {
        return FunctionCreator { HasMRIVolumeAmenableLesion() }
    }

    private fun hasEvidenceOfCNSHemorrhageByMRICreator(): FunctionCreator {
        return FunctionCreator { HasEvidenceOfCNSHemorrhageByMRI() }
    }

    private fun hasIntratumoralHemorrhageByMRICreator(): FunctionCreator {
        return FunctionCreator { HasIntratumoralHemorrhageByMRI() }
    }

    private fun hasLowRiskOfHemorrhageUponTreatmentCreator(): FunctionCreator {
        return FunctionCreator { HasLowRiskOfHemorrhageUponTreatment() }
    }

    private fun hasSuperScanBoneScanCreator(): FunctionCreator {
        return FunctionCreator { HasSuperScanBoneScan() }
    }

    private fun hasChildPughClassCreator(): FunctionCreator {
        return FunctionCreator { HasChildPughClass() }
    }

    private fun hasBCLCStageCreator(): FunctionCreator {
        return FunctionCreator { HasBCLCStage() }
    }

    private fun hasLeftSidedColorectalTumorCreator(): FunctionCreator {
        return FunctionCreator { HasLeftSidedColorectalTumor(doidModel()) }
    }
}