package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;

import org.jetbrains.annotations.NotNull;

public class TumorRuleMapper extends RuleMapper {

    public TumorRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_SOLID_PRIMARY_TUMOR, hasSolidPrimaryTumorCreator());
        map.put(EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA, hasSolidPrimaryTumorCreatorIncludingLymphomaCreator());
        map.put(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X, hasPrimaryTumorBelongsToDoidTermCreator());
        map.put(EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X, hasCancerOfUnknownPrimaryCreator());
        map.put(EligibilityRule.HAS_CANCER_WITH_NEUROENDOCRINE_COMPONENT, hasCancerWithNeuroendocrineComponentCreator());
        map.put(EligibilityRule.HAS_CANCER_WITH_SMALL_CELL_COMPONENT, hasCancerWithSmallCellComponentCreator());
        map.put(EligibilityRule.HAS_BREAST_CANCER_HORMONE_POSITIVE_AND_HER2_NEGATIVE, hasBreastCancerHormonePositiveHER2NegativeCreator());
        map.put(EligibilityRule.HAS_PROSTATE_CANCER_WITH_SMALL_CELL_COMPONENT, hasProstateCancerWithSmallCellComponentCreator());
        map.put(EligibilityRule.HAS_OVARIAN_CANCER_WITH_MUCINOUS_COMPONENT, hasOvarianCancerWithMucinousComponentCreator());
        map.put(EligibilityRule.HAS_OVARIAN_BORDERLINE_TUMOR, hasOvarianBorderlineTumorCreator());
        map.put(EligibilityRule.HAS_STOMACH_UNDIFFERENTIATED_TUMOR, hasStomachUndifferentiatedTumorCreator());
        map.put(EligibilityRule.HAS_SECONDARY_GLIOBLASTOMA, hasSecondaryGlioblastomaCreator());
        map.put(EligibilityRule.HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE, hasCytologicalDocumentationOfTumorTypeCreator());
        map.put(EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE, hasHistologicalDocumentationOfTumorTypeCreator());
        map.put(EligibilityRule.HAS_STAGE_X, hasTumorStageCreator());
        map.put(EligibilityRule.HAS_LOCALLY_ADVANCED_CANCER, hasLocallyAdvancedCancerCreator());
        map.put(EligibilityRule.HAS_METASTATIC_CANCER, hasMetastaticCancerCreator());
        map.put(EligibilityRule.HAS_UNRESECTABLE_CANCER, hasUnresectableCancerCreator());
        map.put(EligibilityRule.HAS_UNRESECTABLE_STAGE_III_CANCER, hasUnresectableStageIIICancerCreator());
        map.put(EligibilityRule.HAS_RECURRENT_CANCER, hasRecurrentCancerCreator());
        map.put(EligibilityRule.HAS_INCURABLE_CANCER, hasIncurableCancerCreator());
        map.put(EligibilityRule.HAS_ANY_LESION, hasAnyLesionCreator());
        map.put(EligibilityRule.HAS_LIVER_METASTASES, hasLivesMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_CNS_METASTASES, hasKnownCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES, hasKnownActiveCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_BRAIN_METASTASES, hasKnownBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES, hasKnownActiveBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_EXTRACRANIAL_METASTASES, hasExtracranialMetastasesCreator());
        map.put(EligibilityRule.HAS_BONE_METASTASES, hasBoneMetastasesCreator());
        map.put(EligibilityRule.HAS_BONE_METASTASES_ONLY, hasOnlyBoneMetastasesCreator());
        map.put(EligibilityRule.HAS_LUNG_METASTASES, hasLungMetastasesCreator());
        map.put(EligibilityRule.HAS_LYMPH_NODE_METASTASES, hasLymphNodeMetastasesCreator());
        map.put(EligibilityRule.HAS_VISCERAL_METASTASES, hasVisceralMetastasesCreator());
        map.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, hasBiopsyAmenableLesionCreator());
        map.put(EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC, tumorBiopsyTakenBeforeInformedConsentCreator());
        map.put(EligibilityRule.CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS, canProvideFreshSampleForFurtherAnalysisCreator());
        map.put(EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FURTHER_ANALYSIS,
                canProvideSampleForFurtherAnalysisCreator());
        map.put(EligibilityRule.MEETS_SPECIFIC_REQUIREMENTS_REGARDING_BIOPSY, meetsSpecificBiopsyRequirementsCreator());
        map.put(EligibilityRule.HAS_EVALUABLE_DISEASE, hasEvaluableDiseaseCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE, hasMeasurableDiseaseCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, hasMeasurableDiseaseRecistCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA, hasSpecificProgressiveDiseaseCriteriaCreator());
        map.put(EligibilityRule.HAS_INJECTION_AMENABLE_LESION, hasInjectionAmenableLesionCreator());
        map.put(EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION, hasMRIVolumeAmenableLesionCreator());
        map.put(EligibilityRule.HAS_EVIDENCE_OF_CNS_HEMORRHAGE_BY_MRI, hasEvidenceOfCNSHemorrhageByMRICreator());
        map.put(EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI, hasIntratumoralHemorrhageByMRICreator());
        map.put(EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT, hasLowRiskOfHemorrhageUponTreatmentCreator());
        map.put(EligibilityRule.HAS_SUPERSCAN_BONE_SCAN, hasSuperScanBoneScanCreator());
        map.put(EligibilityRule.HAS_CHILD_PUGH_CLASS_X_LIVER_SCORE, hasChildPughClassCreator());
        map.put(EligibilityRule.HAS_BCLC_STAGE_X, hasBCLCStageCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasSolidPrimaryTumorCreator() {
        return function -> new HasSolidPrimaryTumor(doidModel());
    }

    @NotNull
    private FunctionCreator hasSolidPrimaryTumorCreatorIncludingLymphomaCreator() {
        return function -> new HasSolidPrimaryTumorIncludingLymphoma(doidModel());
    }

    @NotNull
    private FunctionCreator hasPrimaryTumorBelongsToDoidTermCreator() {
        return function -> {
            String doidTermToMatch = functionInputResolver().createOneDoidTermInput(function);
            return new PrimaryTumorLocationBelongsToDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToMatch));
        };
    }

    @NotNull
    private FunctionCreator hasCancerOfUnknownPrimaryCreator() {
        return function -> {
            TumorTypeInput categoryOfCUP = functionInputResolver().createOneTumorTypeInput(function);
            return new HasCancerOfUnknownPrimary(doidModel(), categoryOfCUP);
        };
    }

    @NotNull
    private FunctionCreator hasBreastCancerHormonePositiveHER2NegativeCreator() {
        return function -> new HasBreastCancerHormonePositiveHER2Negative(doidModel());
    }

    @NotNull
    private FunctionCreator hasCancerWithNeuroendocrineComponentCreator() {
        return function -> new HasCancerWithNeuroendocrineComponent(doidModel());
    }

    @NotNull
    private FunctionCreator hasCancerWithSmallCellComponentCreator() {
        return function -> new HasCancerWithSmallCellComponent(doidModel());
    }

    @NotNull
    private FunctionCreator hasProstateCancerWithSmallCellComponentCreator() {
        return function -> new HasProstateCancerWithSmallCellComponent(doidModel());
    }

    @NotNull
    private FunctionCreator hasOvarianCancerWithMucinousComponentCreator() {
        return function -> new HasOvarianCancerWithMucinousComponent(doidModel());
    }

    @NotNull
    private FunctionCreator hasOvarianBorderlineTumorCreator() {
        return function -> new HasOvarianBorderlineTumor(doidModel());
    }

    @NotNull
    private FunctionCreator hasStomachUndifferentiatedTumorCreator() {
        return function -> new HasStomachUndifferentiatedTumor(doidModel());
    }

    @NotNull
    private FunctionCreator hasSecondaryGlioblastomaCreator() {
        return function -> new HasSecondaryGlioblastoma(doidModel());
    }

    @NotNull
    private FunctionCreator hasCytologicalDocumentationOfTumorTypeCreator() {
        return function -> new HasCytologicalDocumentationOfTumorType();
    }

    @NotNull
    private FunctionCreator hasHistologicalDocumentationOfTumorTypeCreator() {
        return function -> new HasHistologicalDocumentationOfTumorType();
    }

    @NotNull
    private FunctionCreator hasTumorStageCreator() {
        return function -> {
            TumorStage stageToMatch = functionInputResolver().createOneTumorStageInput(function);
            return new HasTumorStage(new TumorStageDerivationFunction(doidModel()), stageToMatch);
        };
    }

    @NotNull
    private FunctionCreator hasLocallyAdvancedCancerCreator() {
        return function -> new DerivedTumorStageEvaluationFunction(new TumorStageDerivationFunction(doidModel()),
                new HasLocallyAdvancedCancer());
    }

    @NotNull
    private FunctionCreator hasMetastaticCancerCreator() {
        return function -> new DerivedTumorStageEvaluationFunction(new TumorStageDerivationFunction(doidModel()),
                new HasMetastaticCancer(doidModel()));
    }

    @NotNull
    private FunctionCreator hasUnresectableCancerCreator() {
        return function -> new DerivedTumorStageEvaluationFunction(new TumorStageDerivationFunction(doidModel()),
                new HasUnresectableCancer());
    }

    @NotNull
    private FunctionCreator hasUnresectableStageIIICancerCreator() {
        return function -> new DerivedTumorStageEvaluationFunction(new TumorStageDerivationFunction(doidModel()),
                new HasUnresectableStageIIICancer());
    }

    @NotNull
    private FunctionCreator hasRecurrentCancerCreator() {
        return function -> new DerivedTumorStageEvaluationFunction(new TumorStageDerivationFunction(doidModel()), new HasRecurrentCancer());
    }

    @NotNull
    private FunctionCreator hasIncurableCancerCreator() {
        return function -> new DerivedTumorStageEvaluationFunction(new TumorStageDerivationFunction(doidModel()), new HasIncurableCancer());
    }

    @NotNull
    private FunctionCreator hasAnyLesionCreator() {
        return function -> new HasAnyLesion();
    }

    @NotNull
    private FunctionCreator hasLivesMetastasesCreator() {
        return function -> new HasLiverMetastases();
    }

    @NotNull
    private FunctionCreator hasKnownCnsMetastasesCreator() {
        return function -> new HasKnownCnsMetastases();
    }

    @NotNull
    private FunctionCreator hasKnownActiveCnsMetastasesCreator() {
        return function -> new HasKnownActiveCnsMetastases();
    }

    @NotNull
    private FunctionCreator hasKnownBrainMetastasesCreator() {
        return function -> new HasKnownBrainMetastases();
    }

    @NotNull
    private FunctionCreator hasKnownActiveBrainMetastasesCreator() {
        return function -> new HasKnownActiveBrainMetastases();
    }

    @NotNull
    private FunctionCreator hasExtracranialMetastasesCreator() {
        return function -> new HasExtracranialMetastases();
    }

    @NotNull
    private FunctionCreator hasBoneMetastasesCreator() {
        return function -> new HasBoneMetastases();
    }

    @NotNull
    private FunctionCreator hasOnlyBoneMetastasesCreator() {
        return function -> new HasBoneMetastasesOnly();
    }

    @NotNull
    private FunctionCreator hasLungMetastasesCreator() {
        return function -> new HasLungMetastases();
    }

    @NotNull
    private FunctionCreator hasLymphNodeMetastasesCreator() {
        return function -> new HasLymphNodeMetastases();
    }

    @NotNull
    private FunctionCreator hasVisceralMetastasesCreator() {
        return function -> new HasVisceralMetastases();
    }

    @NotNull
    private FunctionCreator hasBiopsyAmenableLesionCreator() {
        return function -> new HasBiopsyAmenableLesion();
    }

    @NotNull
    private FunctionCreator tumorBiopsyTakenBeforeInformedConsentCreator() {
        return function -> new TumorBiopsyTakenBeforeInformedConsent();
    }

    @NotNull
    private FunctionCreator canProvideFreshSampleForFurtherAnalysisCreator() {
        return function -> new CanProvideFreshSampleForFurtherAnalysis();
    }

    @NotNull
    private FunctionCreator canProvideSampleForFurtherAnalysisCreator() {
        return function -> new CanProvideSampleForFurtherAnalysis();
    }

    @NotNull
    private FunctionCreator meetsSpecificBiopsyRequirementsCreator() {
        return function -> new MeetsSpecificBiopsyRequirements();
    }

    @NotNull
    private FunctionCreator hasEvaluableDiseaseCreator() {
        return function -> new HasEvaluableDisease();
    }

    @NotNull
    private FunctionCreator hasMeasurableDiseaseCreator() {
        return function -> new HasMeasurableDisease();
    }

    @NotNull
    private FunctionCreator hasMeasurableDiseaseRecistCreator() {
        return function -> new HasMeasurableDiseaseRecist(doidModel());
    }

    @NotNull
    private FunctionCreator hasSpecificProgressiveDiseaseCriteriaCreator() {
        return function -> new HasSpecificProgressiveDiseaseCriteria();
    }

    @NotNull
    private FunctionCreator hasInjectionAmenableLesionCreator() {
        return function -> new HasInjectionAmenableLesion();
    }

    @NotNull
    private FunctionCreator hasMRIVolumeAmenableLesionCreator() {
        return function -> new HasMRIVolumeAmenableLesion();
    }

    @NotNull
    private FunctionCreator hasEvidenceOfCNSHemorrhageByMRICreator() {
        return function -> new HasEvidenceOfCNSHemorrhageByMRI();
    }

    @NotNull
    private FunctionCreator hasIntratumoralHemorrhageByMRICreator() {
        return function -> new HasIntratumoralHemorrhageByMRI();
    }

    @NotNull
    private FunctionCreator hasLowRiskOfHemorrhageUponTreatmentCreator() {
        return function -> new HasLowRiskOfHemorrhageUponTreatment();
    }

    @NotNull
    private FunctionCreator hasSuperScanBoneScanCreator() {
        return function -> new HasSuperScanBoneScan();
    }

    @NotNull
    private FunctionCreator hasChildPughClassCreator() {
        return function -> new HasChildPughClass();
    }

    @NotNull
    private FunctionCreator hasBCLCStageCreator() {
        return function -> new HasBCLCStage();
    }
}
