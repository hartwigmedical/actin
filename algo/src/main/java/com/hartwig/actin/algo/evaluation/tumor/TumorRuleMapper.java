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
        map.put(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_X, hasPrimaryTumorBelongsToDoidCreator());
        map.put(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X, hasPrimaryTumorBelongsToDoidTermCreator());
        map.put(EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X, hasCancerOfUnknownPrimaryCreator());
        map.put(EligibilityRule.HAS_PROSTATE_CANCER_WITH_SMALL_CELL_HISTOLOGY, hasProstateCancerWithSmallCellHistologyCreator());
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
        map.put(EligibilityRule.HAS_BONE_METASTASES, hasBoneMetastasesCreator());
        map.put(EligibilityRule.HAS_BONE_METASTASES_ONLY, hasOnlyBoneMetastasesCreator());
        map.put(EligibilityRule.HAS_LUNG_METASTASES, hasLungMetastasesCreator());
        map.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, hasBiopsyAmenableLesionCreator());
        map.put(EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC, tumorBiopsyTakenBeforeInformedConsentCreator());
        map.put(EligibilityRule.CAN_PROVIDE_FRESH_TISSUE_SAMPLE_FOR_FFPE_ANALYSIS, canProvideFreshSampleForFFPEAnalysisCreator());
        map.put(EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_FRESH_TISSUE_SAMPLE_FOR_FFPE_ANALYSIS, canProvideSampleForFFPEAnalysisCreator());
        map.put(EligibilityRule.HAS_ASSESSABLE_DISEASE, hasAssessableDiseaseCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE, hasMeasurableDiseaseCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, hasMeasurableDiseaseRecistCreator());
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA, hasSpecificProgressiveDiseaseCriteriaCreator());
        map.put(EligibilityRule.HAS_INJECTION_AMENABLE_LESION, hasInjectionAmenableLesionCreator());
        map.put(EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION, hasMRIVolumeAmenableLesionCreator());
        map.put(EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI, hasIntratumoralHemorrhageByMRICreator());
        map.put(EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT, hasLowRiskOfHemorrhageUponTreatmentCreator());
        map.put(EligibilityRule.HAS_SUPERSCAN_BONE_SCAN, hasSuperScanBoneScanCreator());

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
    private FunctionCreator hasPrimaryTumorBelongsToDoidCreator() {
        return function -> {
            String doidToMatch = functionInputResolver().createOneStringInput(function);
            return new PrimaryTumorLocationBelongsToDoid(doidModel(), doidToMatch, false, false);
        };
    }

    @NotNull
    private FunctionCreator hasPrimaryTumorBelongsToDoidTermCreator() {
        return function -> {
            // TODO Map DOID term to DOID
            String doidTermToMatch = functionInputResolver().createOneDoidTermInput(function);
            return new PrimaryTumorLocationBelongsToDoid(doidModel(), doidTermToMatch, false, false);
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
    private FunctionCreator hasProstateCancerWithSmallCellHistologyCreator() {
        return function -> new HasProstateCancerWithSmallCellHistology(doidModel());
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
            return new HasTumorStage(stageToMatch);
        };
    }

    @NotNull
    private FunctionCreator hasLocallyAdvancedCancerCreator() {
        return function -> new HasLocallyAdvancedCancer();
    }

    @NotNull
    private FunctionCreator hasMetastaticCancerCreator() {
        return function -> new HasMetastaticCancer();
    }

    @NotNull
    private FunctionCreator hasUnresectableCancerCreator() {
        return function -> new HasUnresectableCancer();
    }

    @NotNull
    private FunctionCreator hasUnresectableStageIIICancerCreator() {
        return function -> new HasUnresectableStageIIICancer();
    }

    @NotNull
    private FunctionCreator hasRecurrentCancerCreator() {
        return function -> new HasRecurrentCancer();
    }

    @NotNull
    private FunctionCreator hasIncurableCancerCreator() {
        return function -> new HasIncurableCancer();
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
    private FunctionCreator hasBiopsyAmenableLesionCreator() {
        return function -> new HasBiopsyAmenableLesion();
    }

    @NotNull
    private FunctionCreator tumorBiopsyTakenBeforeInformedConsentCreator() {
        return function -> new TumorBiopsyTakenBeforeInformedConsent();
    }

    @NotNull
    private FunctionCreator canProvideFreshSampleForFFPEAnalysisCreator() {
        return function -> new CanProvideFreshSampleForFFPEAnalysis();
    }

    @NotNull
    private FunctionCreator canProvideSampleForFFPEAnalysisCreator() {
        return function -> new CanProvideSampleForFFPEAnalysis();
    }

    @NotNull
    private FunctionCreator hasAssessableDiseaseCreator() {
        return function -> new HasAssessableDisease();
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
}
