package com.hartwig.actin.treatment.util;

import java.util.StringJoiner;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.interpretation.CompositeRules;

import org.jetbrains.annotations.NotNull;

public final class EligibilityFunctionDisplay {

    private EligibilityFunctionDisplay() {
    }

    @NotNull
    public static String format(@NotNull EligibilityFunction function) {
        String value = function.rule().toString();
        if (CompositeRules.isComposite(function.rule())) {
            value = value + "(";
            StringJoiner joiner = new StringJoiner(", ");
            for (Object input : function.parameters()) {
                joiner.add(format((EligibilityFunction) input));
            }
            value = value + joiner + ")";
        } else if (!function.parameters().isEmpty()) {
            value = value + "[";
            StringJoiner joiner = new StringJoiner(", ");
            for (Object input : function.parameters()) {
                joiner.add((String) input);
            }
            value = value + joiner + "]";
        }
        return value;
    }
}
