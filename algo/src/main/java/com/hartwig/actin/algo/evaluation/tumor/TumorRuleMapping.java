package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Map;

import com.google.common.collect.Maps;
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
        map.put(EligibilityRule.HAS_CNS_METASTASES, hasCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_ACTIVE_CNS_METASTASES, hasActiveCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_SYMPTOMATIC_CNS_METASTASES, hasSymptomaticCnsMetastasesCreator());
        map.put(EligibilityRule.HAS_BRAIN_METASTASES, hasBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_ACTIVE_BRAIN_METASTASES, hasActiveBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_SYMPTOMATIC_BRAIN_METASTASES, hasSymptomaticBrainMetastasesCreator());
        map.put(EligibilityRule.HAS_BONE_METASTASES, hasBoneMetastasesCreator());
        map.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, hasMeasurableDiseaseRecistCreator());
        map.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, hasBiopsyAmenableLesionCreator());

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
    private static FunctionCreator hasCnsMetastasesCreator() {
        return function -> new HasCnsMetastases();
    }

    @NotNull
    private static FunctionCreator hasActiveCnsMetastasesCreator() {
        return function -> new HasActiveCnsMetastases();
    }

    @NotNull
    private static FunctionCreator hasSymptomaticCnsMetastasesCreator() {
        return function -> new HasSymptomaticCnsMetastases();
    }

    @NotNull
    private static FunctionCreator hasBrainMetastasesCreator() {
        return function -> new HasBrainMetastases();
    }

    @NotNull
    private static FunctionCreator hasActiveBrainMetastasesCreator() {
        return function -> new HasActiveBrainMetastases();
    }

    @NotNull
    private static FunctionCreator hasSymptomaticBrainMetastasesCreator() {
        return function -> new HasSymptomaticBrainMetastases();
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
}
