package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.hartwig.actin.clinical.datamodel.VitalFunction;

import org.jetbrains.annotations.NotNull;

final class VitalFunctionFunctions {

    private VitalFunctionFunctions() {
    }

    @NotNull
    public static VitalFunction selectMedianFunction(@NotNull Iterable<VitalFunction> vitalFunctions) {
        List<Double> values = sortedValues(vitalFunctions);

        Double median = values.get((int) Math.ceil(values.size() / 2D) - 1);
        for (VitalFunction vitalFunction : vitalFunctions) {
            if (Doubles.compare(vitalFunction.value(), median) == 0) {
                return vitalFunction;
            }
        }

        throw new IllegalStateException("Could not determine median vital function from " + vitalFunctions);
    }

    public static double determineMedianValue(@NotNull Iterable<VitalFunction> vitalFunctions) {
        List<Double> values = sortedValues(vitalFunctions);

        int index = (int) Math.ceil(values.size() / 2D) - 1;
        if (values.size() % 2 == 0) {
            return 0.5 * (values.get(index) + values.get(index + 1));
        } else {
            return values.get(index);
        }
    }

    @NotNull
    private static List<Double> sortedValues(@NotNull Iterable<VitalFunction> vitalFunctions) {
        List<Double> values = Lists.newArrayList();
        for (VitalFunction vitalFunction : vitalFunctions) {
            values.add(vitalFunction.value());
        }

        values.sort(Comparator.naturalOrder());

        return values;
    }
}
