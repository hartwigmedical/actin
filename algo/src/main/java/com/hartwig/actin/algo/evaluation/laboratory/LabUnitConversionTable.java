package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

final class LabUnitConversionTable {

    @VisibleForTesting
    static final Map<LabMeasurement, Map<LabUnit, Map<LabUnit, Double>>> CONVERSION_MAP = Maps.newHashMap();

    static {
        CONVERSION_MAP.put(LabMeasurement.CREATININE, createCreatinineMap());
    }

    @Nullable
    public static Double findConversionFactor(@NotNull LabMeasurement measurement, @NotNull LabUnit fromUnit, @NotNull LabUnit toUnit) {
        Map<LabUnit, Map<LabUnit, Double>> measurementMap = CONVERSION_MAP.get(measurement);

        if (measurementMap == null) {
            return null;
        }

        Map<LabUnit, Double> unitMap = measurementMap.get(fromUnit);
        return unitMap != null ? unitMap.get(toUnit) : null;
    }

    @NotNull
    private static Map<LabUnit, Map<LabUnit, Double>> createCreatinineMap() {
        Map<LabUnit, Map<LabUnit, Double>> map = Maps.newHashMap();
        Map<LabUnit, Double> mgPerDLMap = Maps.newHashMap();
        mgPerDLMap.put(LabUnit.MICROMOLES_PER_LITER, 88.42);

        Map<LabUnit, Double> mmolPerLMap = Maps.newHashMap();
        mmolPerLMap.put(LabUnit.MILLIGRAMS_PER_DECILITER, 1 / 88.42);

        map.put(LabUnit.MILLIGRAMS_PER_DECILITER, mgPerDLMap);
        map.put(LabUnit.MICROMOLES_PER_LITER, mmolPerLMap);

        return map;
    }

    private LabUnitConversionTable() {
    }
}
