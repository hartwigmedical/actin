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
        CONVERSION_MAP.put(LabMeasurement.CREATININE, createCreatinineConversionMap());
        CONVERSION_MAP.put(LabMeasurement.ALBUMIN, createAlbuminConversionMap());
        CONVERSION_MAP.put(LabMeasurement.LYMPHOCYTES_ABS_EDA, createLymphocytesConversionMap());
        CONVERSION_MAP.put(LabMeasurement.HEMOGLOBIN, createHemoglobinConversionMap());
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
    private static Map<LabUnit, Map<LabUnit, Double>> createCreatinineConversionMap() {
        Map<LabUnit, Map<LabUnit, Double>> map = Maps.newHashMap();

        Map<LabUnit, Double> milligramsPerDeciliterMap = Maps.newHashMap();
        milligramsPerDeciliterMap.put(LabUnit.MICROMOLES_PER_LITER, 88.42);

        Map<LabUnit, Double> micromolesPerLiterMap = Maps.newHashMap();
        micromolesPerLiterMap.put(LabUnit.MILLIGRAMS_PER_DECILITER, 1 / 88.42);

        map.put(LabUnit.MILLIGRAMS_PER_DECILITER, milligramsPerDeciliterMap);
        map.put(LabUnit.MICROMOLES_PER_LITER, micromolesPerLiterMap);

        return map;
    }

    @NotNull
    private static Map<LabUnit, Map<LabUnit, Double>> createAlbuminConversionMap() {
        Map<LabUnit, Map<LabUnit, Double>> map = Maps.newHashMap();

        Map<LabUnit, Double> gramsPerDeciliterMap = Maps.newHashMap();
        gramsPerDeciliterMap.put(LabUnit.GRAMS_PER_LITER, 10D);

        Map<LabUnit, Double> gramsPerLiterMap = Maps.newHashMap();
        gramsPerLiterMap.put(LabUnit.GRAMS_PER_DECILITER, 1 / 10D);

        map.put(LabUnit.GRAMS_PER_DECILITER, gramsPerDeciliterMap);
        map.put(LabUnit.GRAMS_PER_LITER, gramsPerLiterMap);

        return map;
    }

    @NotNull
    private static Map<LabUnit, Map<LabUnit, Double>> createLymphocytesConversionMap() {
        Map<LabUnit, Map<LabUnit, Double>> map = Maps.newHashMap();

        Map<LabUnit, Double> cellsPerCubicMillimeterMap = Maps.newHashMap();
        cellsPerCubicMillimeterMap.put(LabUnit.BILLIONS_PER_LITER, 0.001);

        Map<LabUnit, Double> billionsPerLiterMap = Maps.newHashMap();
        billionsPerLiterMap.put(LabUnit.CELLS_PER_CUBIC_MILLIMETER, 1 / 0.001);

        map.put(LabUnit.CELLS_PER_CUBIC_MILLIMETER, cellsPerCubicMillimeterMap);
        map.put(LabUnit.BILLIONS_PER_LITER, billionsPerLiterMap);

        return map;
    }

    @NotNull
    private static Map<LabUnit, Map<LabUnit, Double>> createHemoglobinConversionMap() {
        Map<LabUnit, Map<LabUnit, Double>> map = Maps.newHashMap();

        Map<LabUnit, Double> gramsPerDeciliterMap = Maps.newHashMap();
        gramsPerDeciliterMap.put(LabUnit.MILLIMOLES_PER_LITER, 0.6206);

        Map<LabUnit, Double> millimolesPerLiterMap = Maps.newHashMap();
        millimolesPerLiterMap.put(LabUnit.GRAMS_PER_DECILITER, 1 / 0.6206);

        map.put(LabUnit.GRAMS_PER_DECILITER, gramsPerDeciliterMap);
        map.put(LabUnit.MILLIMOLES_PER_LITER, millimolesPerLiterMap);

        return map;
    }

    private LabUnitConversionTable() {
    }
}
