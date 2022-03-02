package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.LabUnit;

import org.jetbrains.annotations.NotNull;

final class LabUnitConversionTable {

    private static final Map<LabUnit, Map<LabUnit, Double>> CONVERSION_MAP = Maps.newHashMap();

    static {
        Map<LabUnit, Double> mgPerDLMap = Maps.newHashMap();
        mgPerDLMap.put(LabUnit.MICROMOLES_PER_LITER, 88.42);

        Map<LabUnit, Double> mmolPerLMap = Maps.newHashMap();
        mmolPerLMap.put(LabUnit.MILLIGRAMS_PER_DECILITER, 1 / 88.42);

        CONVERSION_MAP.put(LabUnit.MILLIGRAMS_PER_DECILITER, mgPerDLMap);
        CONVERSION_MAP.put(LabUnit.MICROMOLES_PER_LITER, mmolPerLMap);
    }

    @Nullable
    public static Double findConversionFactor(@NotNull LabUnit fromUnit, @NotNull LabUnit toUnit) {
        Map<LabUnit, Double> unitMap = CONVERSION_MAP.get(fromUnit);

        return unitMap != null ? unitMap.get(toUnit) : null;
    }

    private LabUnitConversionTable() {
    }
}
