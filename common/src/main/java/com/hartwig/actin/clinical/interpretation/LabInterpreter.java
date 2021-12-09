package com.hartwig.actin.clinical.interpretation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public final class LabInterpreter {

    static final Map<LabMeasurement, LabMeasurement> MAPPINGS = Maps.newHashMap();

    static {
        MAPPINGS.put(LabMeasurement.NEUTROPHILS_ABS_EDA, LabMeasurement.NEUTROPHILS_ABS);
    }

    private LabInterpreter() {
    }

    @NotNull
    public static LabInterpretation interpret(@NotNull List<LabValue> labValues) {
        Multimap<LabMeasurement, LabValue> measurements = ArrayListMultimap.create();
        for (LabMeasurement measurement : LabMeasurement.values()) {
            measurements.putAll(measurement, filterByCode(labValues, measurement.code()));
        }

        for (Map.Entry<LabMeasurement, LabMeasurement> mapping : MAPPINGS.entrySet()) {
            for (LabValue labValue : measurements.get(mapping.getKey())) {
                measurements.put(mapping.getValue(), convert(labValue, mapping.getValue()));
            }
        }

        return LabInterpretation.fromMeasurements(measurements);
    }

    @NotNull
    private static LabValue convert(@NotNull LabValue labValue, @NotNull LabMeasurement targetMeasure) {
        return ImmutableLabValue.builder().from(labValue).code(targetMeasure.code()).unit(targetMeasure.expectedUnit()).build();
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
