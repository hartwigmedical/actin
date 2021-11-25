package com.hartwig.actin.clinical.interpretation;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public final class LabInterpreter {

    private LabInterpreter() {
    }

    @NotNull
    public static LabInterpretation interpret(@NotNull List<LabValue> labValues) {
        Multimap<LabMeasurement, LabValue> labValuesMap = ArrayListMultimap.create();
        for (LabMeasurement measurement : LabMeasurement.values()) {
            labValuesMap.putAll(measurement, filterByCode(labValues, measurement.code()));
        }

        return new LabInterpretation(labValuesMap);
    }

    @NotNull
    private static List<LabValue> filterByCode(@NotNull List<LabValue> labValues, @NotNull String code) {
        List<LabValue> filtered = Lists.newArrayList();
        for (LabValue labValue : labValues) {
            if (labValue.code().equals(code)) {
                filtered.add(labValue);
            }
        }
        return filtered;
    }
}
