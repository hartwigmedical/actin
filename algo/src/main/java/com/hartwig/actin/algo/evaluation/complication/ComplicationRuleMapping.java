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

        return map;
    }

    @NotNull
    private static FunctionCreator hasSpecificComplicationCreator() {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasSpecificComplication(termToFind);
        };
    }
}
