package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;

import org.jetbrains.annotations.NotNull;

public final class TumorRuleMapping {

    private TumorRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_SOLID_PRIMARY_TUMOR, hasSolidPrimaryTumorCreator(doidModel));
        map.put(EligibilityRule.HAS_SOLID_PRIMARY_TUMOR_INCLUDING_LYMPHOMA, hasSolidPrimaryTumorCreatorIncludingLymphomaCreator());
        map.put(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_X, hasPrimaryTumorBelongsToDoidCreator(doidModel));
        map.put(EligibilityRule.HAS_CANCER_OF_UNKNOWN_PRIMARY_AND_TYPE_X, hasCancerOfUnknownPrimaryCreator(doidModel));
        map.put(EligibilityRule.HAS_PROSTATE_CANCER_WITH_SMALL_CELL_HISTOLOGY, hasProstateCancerWithSmallCellHistologyCreator(doidModel));
        map.put(EligibilityRule.HAS_CYTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE, hasCytologicalDocumentationOfTumorTypeCreator());
        map.put(EligibilityRule.HAS_HISTOLOGICAL_DOCUMENTATION_OF_TUMOR_TYPE, hasHistologicalDocumentationOfTumorTypeCreator());
        map.put(EligibilityRule.HAS_STAGE_X, hasTumorStageCreator());
        map.put(EligibilityRule.HAS_ADVANCED_CANCER, hasAdvancedCancerCreator());
        map.put(EligibilityRule.HAS_METASTATIC_CANCER, hasMetastaticCancerCreator());
        map.put(EligibilityRule.HAS_UNRESECTABLE_CANCER, hasUnresectableCancerCreator());
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
        map.put(EligibilityRule.CAN_PROVIDE_ARCHIVAL_OR_NEW_TISSUE_SAMPLE_FOR_FFPE_ANALYSIS, canProvideSampleForFFPEAnalysisCreator());
        map.put(EligibilityRule.HAS_ASSESSABLE_DISEASE, hasAssessableDiseaseCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE, hasMeasurableDiseaseCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, hasMeasurableDiseaseRecistCreator(doidModel));
        map.put(EligibilityRule.HAS_PROGRESSIVE_DISEASE_ACCORDING_TO_SPECIFIC_CRITERIA, hasSpecificProgressiveDiseaseCriteriaCreator());
        map.put(EligibilityRule.HAS_INJECTION_AMENABLE_LESION, hasInjectionAmenableLesionCreator());
        map.put(EligibilityRule.HAS_MRI_VOLUME_MEASUREMENT_AMENABLE_LESION, hasMRIVolumeAmenableLesionCreator());
        map.put(EligibilityRule.HAS_INTRATUMORAL_HEMORRHAGE_BY_MRI, hasIntratumoralHemorrhageByMRICreator());
        map.put(EligibilityRule.HAS_LOW_RISK_OF_HEMORRHAGE_UPON_TREATMENT, hasLowRiskOfHemorrhageUponTreatmentCreator());
        map.put(EligibilityRule.HAS_SUPERSCAN_BONE_SCAN, hasSuperScanBoneScanCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSolidPrimaryTumorCreator(@NotNull DoidModel doidModel) {
        return function -> new HasSolidPrimaryTumor(doidModel);
    }

    @NotNull
    private static FunctionCreator hasSolidPrimaryTumorCreatorIncludingLymphomaCreator() {
        return function -> new HasSolidPrimaryTumorCreatorIncludingLymphoma();
    }

    @NotNull
    private static FunctionCreator hasPrimaryTumorBelongsToDoidCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doidToMatch = FunctionInputResolver.createOneStringInput(function);
            return new PrimaryTumorLocationBelongsToDoid(doidModel, doidToMatch, false, false);
        };
    }

    @NotNull
    private static FunctionCreator hasCancerOfUnknownPrimaryCreator(@NotNull DoidModel doidModel) {
        return function -> {
            TumorTypeInput categoryOfCUP = FunctionInputResolver.createOneTumorTypeInput(function);
            return new HasCancerOfUnknownPrimary(doidModel, categoryOfCUP);
        };
    }

    @NotNull
    private static FunctionCreator hasProstateCancerWithSmallCellHistologyCreator(@NotNull DoidModel doidModel) {
        return function -> new HasProstateCancerWithSmallCellHistology(doidModel);
    }

    @NotNull
    private static FunctionCreator hasCytologicalDocumentationOfTumorTypeCreator() {
        return function -> new HasCytologicalDocumentationOfTumorType();
    }

    @NotNull
    private static FunctionCreator hasHistologicalDocumentationOfTumorTypeCreator() {
        return function -> new HasHistologicalDocumentationOfTumorType();
    }

    @NotNull
    private static FunctionCreator hasTumorStageCreator() {
        return function -> {
            TumorStage stageToMatch = FunctionInputResolver.createOneTumorStageInput(function);
            return new HasTumorStage(stageToMatch);
        };
    }

    @NotNull
    private static FunctionCreator hasAdvancedCancerCreator() {
        return function -> new HasAdvancedCancer();
    }

    @NotNull
    private static FunctionCreator hasMetastaticCancerCreator() {
        return function -> new HasMetastaticCancer();
    }

    @NotNull
    private static FunctionCreator hasUnresectableCancerCreator() {
        return function -> new HasUnresectableCancer();
    }

    @NotNull
    private static FunctionCreator hasAnyLesionCreator() {
        return function -> new HasAnyLesion();
    }

    @NotNull
    private static FunctionCreator hasLivesMetastasesCreator() {
        return function -> new HasLiverMetastases();
    }

    @NotNull
    private static FunctionCreator hasKnownCnsMetastasesCreator() {
        return function -> new HasKnownCnsMetastases();
    }

    @NotNull
    private static FunctionCreator hasKnownActiveCnsMetastasesCreator() {
        return function -> new HasKnownActiveCnsMetastases();
    }

    @NotNull
    private static FunctionCreator hasKnownBrainMetastasesCreator() {
        return function -> new HasKnownBrainMetastases();
    }

    @NotNull
    private static FunctionCreator hasKnownActiveBrainMetastasesCreator() {
        return function -> new HasKnownActiveBrainMetastases();
    }

    @NotNull
    private static FunctionCreator hasBoneMetastasesCreator() {
        return function -> new HasBoneMetastases();
    }

    @NotNull
    private static FunctionCreator hasOnlyBoneMetastasesCreator() {
        return function -> new HasBoneMetastasesOnly();
    }

    @NotNull
    private static FunctionCreator hasLungMetastasesCreator() {
        return function -> new HasLungMetastases();
    }

    @NotNull
    private static FunctionCreator hasBiopsyAmenableLesionCreator() {
        return function -> new HasBiopsyAmenableLesion();
    }

    @NotNull
    private static FunctionCreator tumorBiopsyTakenBeforeInformedConsentCreator() {
        return function -> new TumorBiopsyTakenBeforeInformedConsent();
    }

    @NotNull
    private static FunctionCreator canProvideSampleForFFPEAnalysisCreator() {
        return function -> new CanProvideSampleForFFPEAnalysis();
    }

    @NotNull
    private static FunctionCreator hasAssessableDiseaseCreator() {
        return function -> new HasAssessableDisease();
    }

    @NotNull
    private static FunctionCreator hasMeasurableDiseaseCreator() {
        return function -> new HasMeasurableDisease();
    }

    @NotNull
    private static FunctionCreator hasMeasurableDiseaseRecistCreator(@NotNull DoidModel doidModel) {
        return function -> new HasMeasurableDiseaseRecist(doidModel);
    }

    @NotNull
    private static FunctionCreator hasSpecificProgressiveDiseaseCriteriaCreator() {
        return function -> new HasSpecificProgressiveDiseaseCriteria();
    }

    @NotNull
    private static FunctionCreator hasInjectionAmenableLesionCreator() {
        return function -> new HasInjectionAmenableLesion();
    }

    @NotNull
    private static FunctionCreator hasMRIVolumeAmenableLesionCreator() {
        return function -> new HasMRIVolumeAmenableLesion();
    }

    @NotNull
    private static FunctionCreator hasIntratumoralHemorrhageByMRICreator() {
        return function -> new HasIntratumoralHemorrhageByMRI();
    }

    @NotNull
    private static FunctionCreator hasLowRiskOfHemorrhageUponTreatmentCreator() {
        return function -> new HasLowRiskOfHemorrhageUponTreatment();
    }

    @NotNull
    private static FunctionCreator hasSuperScanBoneScanCreator() {
        return function -> new HasSuperScanBoneScan();
    }
}
