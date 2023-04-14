package com.hartwig.actin.soc.evaluation.tumor

import com.google.common.collect.Maps

class TumorRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        val map: MutableMap<EligibilityRule, FunctionCreator> = Maps.newHashMap()
        map[EligibilityRule.HAS_SOLID_PRIMARY_TUMOR] = hasSolidPrimaryTumorCreator()
        map[EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA] = hasSolidPrimaryTumorCreatorIncludingLymphomaCreator()
        map[EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X] = hasPrimaryTumorBelongsToDoidTermCreator()
        map[EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X] = hasCancerOfUnknownPrimaryCreator()
        map[EligibilityRule.HAS_CANCER_WITH_NEUROENDOCRINE_COMPONENT] = hasCancerWithNeuroendocrineComponentCreator()
        map[EligibilityRule.HAS_CANCER_WITH_SMALL_CELL_COMPONENT] = hasCancerWithSmallCellComponentCreator()
        map[EligibilityRule.HAS_BREAST_CANCER_HORMONE_POSITIVE_AND_HER2_NEGATIVE] = hasBreastCancerHormonePositiveHER2NegativeCreator()
        map[EligibilityRule.HAS_PROSTATE_CANCER_WITH_SMALL_CELL_COMPONENT] = hasProstateCancerWithSmallCellComponentCreator()
        map[EligibilityRule.HAS_OVARIAN_CANCER_WITH_MUCINOUS_COMPONENT] = hasOvarianCancerWithMucinousComponentCreator()
        map[EligibilityRule.HAS_OVARIAN_BORDERLINE_TUMOR] = hasOvarianBorderlineTumorCreator()
        map[EligibilityRule.HAS_STOMACH_UNDIFFERENTIATED_TUMOR] = hasStomachUndifferentiatedTumorCreator()
        map[EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA] = hasSecondaryGlioblastomaCreator()
        map[EligibilityRule.HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE] = hasCytologicalDocumentationOfTumorTypeCreator()
        map[EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE] = hasHistologicalDocumentationOfTumorTypeCreator()
        map[EligibilityRule.HAS_STAGE_X] = hasTumorStageCreator()
        map[EligibilityRule.HAS_LOCALLY_ADVANCED_CANCER] = hasLocallyAdvancedCancerCreator()
        map[EligibilityRule.HAS_METASTATIC_CANCER] = hasMetastaticCancerCreator()
        map[EligibilityRule.HAS_UNRESECTABLE_CANCER] = hasUnresectableCancerCreator()
        map[EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER] = hasUnresectableStageIIICancerCreator()
        map[EligibilityRule.HAS_RECURRENT_CANCER] = hasRecurrentCancerCreator()
        map[EligibilityRule.HAS_INCURABLE_CANCER] = hasIncurableCancerCreator()
        map[EligibilityRule.HAS_ANY_LESION] = hasAnyLesionCreator()
        map[EligibilityRule.HAS_LIVER_METASTASES] = hasLivesMetastasesCreator()
        map[EligibilityRule.HAS_KNOWN_CNS_METASTASES] = hasKnownCnsMetastasesCreator()
        map[EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES] = hasKnownActiveCnsMetastasesCreator()
        map[EligibilityRule.HAS_KNOWN_BRAIN_METASTASES] = hasKnownBrainMetastasesCreator()
        map[EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES] = hasKnownActiveBrainMetastasesCreator()
        map[EligibilityRule.HAS_EXTRACRANIAL_METASTASES] = hasExtracranialMetastasesCreator()
        map[EligibilityRule.HAS_BONE_METASTASES] = hasBoneMetastasesCreator()
        map[EligibilityRule.HAS_BONE_METASTASES_ONLY] = hasOnlyBoneMetastasesCreator()
        map[EligibilityRule.HAS_LUNG_METASTASES] = hasLungMetastasesCreator()
        map[EligibilityRule.HAS_LYMPH_NODE_METASTASES] = hasLymphNodeMetastasesCreator()
        map[EligibilityRule.HAS_VISCERAL_METASTASES] = hasVisceralMetastasesCreator()
        map[EligibilityRule.HAS_BIOPSY_AMENABLE_LESION] = hasBiopsyAmenableLesionCreator()
        map[EligibilityRule.HAS_PRESENCE_OF_LESIONS_IN_AT_LEAST_X_SITES] = hasMinimumSitesWithLesionsCreator()
        map[EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC] = tumorBiopsyTakenBeforeInformedConsentCreator()
        map[EligibilityRule.CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS] = canProvideFreshSampleForFurtherAnalysisCreator()
        map[EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS] = canProvideSampleForFurtherAnalysisCreator()
        map[EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY] = meetsSpecificBiopsyRequirementsCreator()
        map[EligibilityRule.HAS_EVALUABLE_DISEASE] = hasEvaluableDiseaseCreator()
        map[EligibilityRule.HAS_MEASURABLE_DISEASE] = hasMeasurableDiseaseCreator()
        map[EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST] = hasMeasurableDiseaseRecistCreator()
        map[EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA] = hasSpecificProgressiveDiseaseCriteriaCreator()
        map[EligibilityRule.HAS_INJECTION_AMENABLE_LESION] = hasInjectionAmenableLesionCreator()
        map[EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION] = hasMRIVolumeAmenableLesionCreator()
        map[EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI] = hasEvidenceOfCNSHemorrhageByMRICreator()
        map[EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI] = hasIntratumoralHemorrhageByMRICreator()
        map[EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT] = hasLowRiskOfHemorrhageUponTreatmentCreator()
        map[EligibilityRule.HAS_SUPERSCAN_BONE_SCAN] = hasSuperScanBoneScanCreator()
        map[EligibilityRule.HAS_CHILD_PUGH_CLASS_X_LIVER_SCORE] = hasChildPughClassCreator()
        map[EligibilityRule.HAS_BCLC_STAGE_X] = hasBCLCStageCreator()
        map[EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR] = hasLeftSidedColorectalTumorCreator()
        return map
    }

    private fun hasSolidPrimaryTumorCreator(): FunctionCreator {
        return FunctionCreator { function -> HasSolidPrimaryTumor(doidModel()) }
    }

    private fun hasSolidPrimaryTumorCreatorIncludingLymphomaCreator(): FunctionCreator {
        return FunctionCreator { function -> HasSolidPrimaryTumorIncludingLymphoma(doidModel()) }
    }

    private fun hasPrimaryTumorBelongsToDoidTermCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val doidTermToMatch: String = functionInputResolver().createOneDoidTermInput(function)
            PrimaryTumorLocationBelongsToDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToMatch))
        }
    }

    private fun hasCancerOfUnknownPrimaryCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val categoryOfCUP: TumorTypeInput = functionInputResolver().createOneTumorTypeInput(function)
            HasCancerOfUnknownPrimary(doidModel(), categoryOfCUP)
        }
    }

    private fun hasBreastCancerHormonePositiveHER2NegativeCreator(): FunctionCreator {
        return FunctionCreator { function -> HasBreastCancerHormonePositiveHER2Negative(doidModel()) }
    }

    private fun hasCancerWithNeuroendocrineComponentCreator(): FunctionCreator {
        return FunctionCreator { function -> HasCancerWithNeuroendocrineComponent(doidModel()) }
    }

    private fun hasCancerWithSmallCellComponentCreator(): FunctionCreator {
        return FunctionCreator { function -> HasCancerWithSmallCellComponent(doidModel()) }
    }

    private fun hasProstateCancerWithSmallCellComponentCreator(): FunctionCreator {
        return FunctionCreator { function -> HasProstateCancerWithSmallCellComponent(doidModel()) }
    }

    private fun hasOvarianCancerWithMucinousComponentCreator(): FunctionCreator {
        return FunctionCreator { function -> HasOvarianCancerWithMucinousComponent(doidModel()) }
    }

    private fun hasOvarianBorderlineTumorCreator(): FunctionCreator {
        return FunctionCreator { function -> HasOvarianBorderlineTumor(doidModel()) }
    }

    private fun hasStomachUndifferentiatedTumorCreator(): FunctionCreator {
        return FunctionCreator { function -> HasStomachUndifferentiatedTumor(doidModel()) }
    }

    private fun hasSecondaryGlioblastomaCreator(): FunctionCreator {
        return FunctionCreator { function -> HasSecondaryGlioblastoma(doidModel()) }
    }

    private fun hasCytologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return FunctionCreator { function -> HasCytologicalDocumentationOfTumorType() }
    }

    private fun hasHistologicalDocumentationOfTumorTypeCreator(): FunctionCreator {
        return FunctionCreator { function -> HasHistologicalDocumentationOfTumorType() }
    }

    private fun hasTumorStageCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val stageToMatch: TumorStage = functionInputResolver().createOneTumorStageInput(function)
            HasTumorStage(TumorStageDerivationFunction.create(doidModel()), stageToMatch)
        }
    }

    private fun hasLocallyAdvancedCancerCreator(): FunctionCreator {
        return FunctionCreator { function ->
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()),
                    HasLocallyAdvancedCancer())
        }
    }

    private fun hasMetastaticCancerCreator(): FunctionCreator {
        return FunctionCreator { function ->
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()),
                    HasMetastaticCancer(doidModel()))
        }
    }

    private fun hasUnresectableCancerCreator(): FunctionCreator {
        return FunctionCreator { function ->
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()),
                    HasUnresectableCancer())
        }
    }

    private fun hasUnresectableStageIIICancerCreator(): FunctionCreator {
        return FunctionCreator { function ->
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()),
                    HasUnresectableStageIIICancer())
        }
    }

    private fun hasRecurrentCancerCreator(): FunctionCreator {
        return FunctionCreator { function ->
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()),
                    HasRecurrentCancer())
        }
    }

    private fun hasIncurableCancerCreator(): FunctionCreator {
        return FunctionCreator { function ->
            DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction.create(doidModel()),
                    HasIncurableCancer())
        }
    }

    private fun hasAnyLesionCreator(): FunctionCreator {
        return FunctionCreator { function -> HasAnyLesion() }
    }

    private fun hasLivesMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasLiverMetastases() }
    }

    private fun hasKnownCnsMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasKnownCnsMetastases() }
    }

    private fun hasKnownActiveCnsMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasKnownActiveCnsMetastases() }
    }

    private fun hasKnownBrainMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasKnownBrainMetastases() }
    }

    private fun hasKnownActiveBrainMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasKnownActiveBrainMetastases() }
    }

    private fun hasExtracranialMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasExtracranialMetastases() }
    }

    private fun hasBoneMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasBoneMetastases() }
    }

    private fun hasOnlyBoneMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasBoneMetastasesOnly() }
    }

    private fun hasLungMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasLungMetastases() }
    }

    private fun hasLymphNodeMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasLymphNodeMetastases() }
    }

    private fun hasVisceralMetastasesCreator(): FunctionCreator {
        return FunctionCreator { function -> HasVisceralMetastases() }
    }

    private fun hasBiopsyAmenableLesionCreator(): FunctionCreator {
        return FunctionCreator { function -> HasBiopsyAmenableLesion() }
    }

    private fun hasMinimumSitesWithLesionsCreator(): FunctionCreator {
        return FunctionCreator { function -> HasMinimumSitesWithLesions(functionInputResolver().createOneIntegerInput(function)) }
    }

    private fun tumorBiopsyTakenBeforeInformedConsentCreator(): FunctionCreator {
        return FunctionCreator { function -> TumorBiopsyTakenBeforeInformedConsent() }
    }

    private fun canProvideFreshSampleForFurtherAnalysisCreator(): FunctionCreator {
        return FunctionCreator { function -> CanProvideFreshSampleForFurtherAnalysis() }
    }

    private fun canProvideSampleForFurtherAnalysisCreator(): FunctionCreator {
        return FunctionCreator { function -> CanProvideSampleForFurtherAnalysis() }
    }

    private fun meetsSpecificBiopsyRequirementsCreator(): FunctionCreator {
        return FunctionCreator { function -> MeetsSpecificBiopsyRequirements() }
    }

    private fun hasEvaluableDiseaseCreator(): FunctionCreator {
        return FunctionCreator { function -> HasEvaluableDisease() }
    }

    private fun hasMeasurableDiseaseCreator(): FunctionCreator {
        return FunctionCreator { function -> HasMeasurableDisease() }
    }

    private fun hasMeasurableDiseaseRecistCreator(): FunctionCreator {
        return FunctionCreator { function -> HasMeasurableDiseaseRecist(doidModel()) }
    }

    private fun hasSpecificProgressiveDiseaseCriteriaCreator(): FunctionCreator {
        return FunctionCreator { function -> HasSpecificProgressiveDiseaseCriteria() }
    }

    private fun hasInjectionAmenableLesionCreator(): FunctionCreator {
        return FunctionCreator { function -> HasInjectionAmenableLesion() }
    }

    private fun hasMRIVolumeAmenableLesionCreator(): FunctionCreator {
        return FunctionCreator { function -> HasMRIVolumeAmenableLesion() }
    }

    private fun hasEvidenceOfCNSHemorrhageByMRICreator(): FunctionCreator {
        return FunctionCreator { function -> HasEvidenceOfCNSHemorrhageByMRI() }
    }

    private fun hasIntratumoralHemorrhageByMRICreator(): FunctionCreator {
        return FunctionCreator { function -> HasIntratumoralHemorrhageByMRI() }
    }

    private fun hasLowRiskOfHemorrhageUponTreatmentCreator(): FunctionCreator {
        return FunctionCreator { function -> HasLowRiskOfHemorrhageUponTreatment() }
    }

    private fun hasSuperScanBoneScanCreator(): FunctionCreator {
        return FunctionCreator { function -> HasSuperScanBoneScan() }
    }

    private fun hasChildPughClassCreator(): FunctionCreator {
        return FunctionCreator { function -> HasChildPughClass() }
    }

    private fun hasBCLCStageCreator(): FunctionCreator {
        return FunctionCreator { function -> HasBCLCStage() }
    }

    private fun hasLeftSidedColorectalTumorCreator(): FunctionCreator {
        return FunctionCreator { function -> HasLeftSidedColorectalTumor(doidModel()) }
    }
}