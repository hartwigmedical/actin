package com.hartwig.actin.algo.evaluation.complication;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class ComplicationRuleMapping {

    private ComplicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_COMPLICATION_X, hasSpecificComplicationCreator());
        map.put(EligibilityRule.HAS_UNCONTROLLED_TUMOR_RELATED_PAIN, hasUncontrolledTumorRelatedPainCreator());
        map.put(EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE, hasLeptomeningealDiseaseCreator());
        map.put(EligibilityRule.HAS_SPINAL_CORD_COMPRESSION, hasSpinalCordCompressionCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSpecificComplicationCreator() {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasSpecificComplication(termToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasUncontrolledTumorRelatedPainCreator() {
        return function -> new HasUncontrolledTumorRelatedPain();
    }

    @NotNull
    private static FunctionCreator hasLeptomeningealDiseaseCreator() {
        return function -> new HasLeptomeningealDisease();
    }

    @NotNull
    private static FunctionCreator hasSpinalCordCompressionCreator() {
        return function -> new HasSpinalCordCompression();
    }

}
