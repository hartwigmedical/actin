package com.hartwig.actin.clinical.feed.lab;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.hartwig.actin.clinical.datamodel.LabUnit;

import org.jetbrains.annotations.NotNull;

final class LabUnitResolver {

    @VisibleForTesting
    static final Map<String, LabUnit> CURATION_MAP = Maps.newHashMap();

    static {
        CURATION_MAP.put("10*6/L", LabUnit.MILLIONS_PER_LITER);
        CURATION_MAP.put("10*9/L", LabUnit.BILLIONS_PER_LITER);
        CURATION_MAP.put("10*12/L", LabUnit.TRILLIONS_PER_LITER);
        CURATION_MAP.put("% van de leukocyten", LabUnit.PERCENTAGE_OF_LEUKOCYTES);
        CURATION_MAP.put("% van de T-cellen", LabUnit.PERCENTAGE_OF_T_CELLS);

        CURATION_MAP.put("mmol/mol Kreatinine", LabUnit.MILLIMOLES_PER_MOLE);
        CURATION_MAP.put("µg/L", LabUnit.MICROGRAMS_PER_LITER);
        CURATION_MAP.put("µmol/L", LabUnit.MICROMOLES_PER_LITER);
        CURATION_MAP.put("micromol/l", LabUnit.MICROMOLES_PER_LITER);
        CURATION_MAP.put("E/ml", LabUnit.UNITS_PER_MILLILITER);

        // L/L is an implied unit used for hematocrit
        CURATION_MAP.put("L/L", LabUnit.NONE);

        CURATION_MAP.put("mol/mol", LabUnit.NONE);
    }

    private LabUnitResolver() {
    }

    @NotNull
    public static LabUnit resolve(@NotNull String unit) {
        if (CURATION_MAP.containsKey(unit)) {
            return CURATION_MAP.get(unit);
        }

        for (LabUnit labUnit : LabUnit.values()) {
            if (labUnit.display().equalsIgnoreCase(unit)) {
                return labUnit;
            }
        }

        throw new IllegalStateException("Could not map lab unit: '" + unit + "'");
    }
}
