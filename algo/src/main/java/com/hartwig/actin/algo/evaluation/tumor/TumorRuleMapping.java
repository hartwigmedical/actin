package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.jetbrains.annotations.NotNull;

public final class TumorRuleMapping {

    private TumorRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X, primaryTumorLocationBelongsToDoidCreator(doidModel));
        map.put(EligibilityRule.HAS_ADVANCED_CANCER, hasAdvancedCancerCreator());
        map.put(EligibilityRule.HAS_METASTATIC_CANCER, hasMetastaticCancerCreator());
        map.put(EligibilityRule.HAS_LIVER_METASTASES, hasLivesMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_CNS_METASTASES, hasKnownCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES, hasKnownActiveCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_CNS_METASTASES, hasKnownSymptomaticCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_BRAIN_METASTASES, hasKnownBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES, hasKnownActiveBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_BRAIN_METASTASES, hasKnownSymptomaticBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_BONE_METASTASES, hasBoneMetastasesCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, hasMeasurableDiseaseRecistCreator());
        map.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, hasBiopsyAmenableLesionCreator());
        map.put(EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC, notImplementedCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator primaryTumorLocationBelongsToDoidCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doid = EligibilityParameterResolver.createOneStringParameter(function);
            return new PrimaryTumorLocationBelongsToDoid(doidModel, doid);
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
    private static FunctionCreator hasKnownSymptomaticCnsMetastasesCreator() {
        return function -> new HasKnownSymptomaticCnsMetastases();
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
    private static FunctionCreator hasKnownSymptomaticBrainMetastasesCreator() {
        return function -> new HasKnownSymptomaticBrainMetastases();
    }

    @NotNull
    private static FunctionCreator hasBoneMetastasesCreator() {
        return function -> new HasBoneMetastases();
    }

    @NotNull
    private static FunctionCreator hasMeasurableDiseaseRecistCreator() {
        return function -> new HasMeasurableDiseaseRecist();
    }

    @NotNull
    private static FunctionCreator hasBiopsyAmenableLesionCreator() {
        return function -> new HasBiopsyAmenableLesion();
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
