package com.hartwig.actin.clinical.feed.emc.lab

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Maps
import com.hartwig.actin.datamodel.clinical.LabUnit

internal object LabUnitResolver {
    @VisibleForTesting
    val CURATION_MAP: MutableMap<String, LabUnit> = Maps.newHashMap()

    init {
        CURATION_MAP["10*6/L"] = LabUnit.MILLIONS_PER_LITER
        CURATION_MAP["*10E6/l"] = LabUnit.MILLIONS_PER_LITER
        CURATION_MAP["10*9/L"] = LabUnit.BILLIONS_PER_LITER
        CURATION_MAP["*10E9/l"] = LabUnit.BILLIONS_PER_LITER
        CURATION_MAP["10*12/L"] = LabUnit.TRILLIONS_PER_LITER
        CURATION_MAP["% van de leukocyten"] = LabUnit.PERCENTAGE_OF_LEUKOCYTES
        CURATION_MAP["% van de T-cellen"] = LabUnit.PERCENTAGE_OF_T_CELLS
        CURATION_MAP["mmol/mol Kreatinine"] = LabUnit.MILLIMOLES_PER_MOLE
        CURATION_MAP["g/mol Kreatinine"] = LabUnit.GRAMS_PER_MOLE
        CURATION_MAP["µg/L"] = LabUnit.MICROGRAMS_PER_LITER
        CURATION_MAP["µmol/L"] = LabUnit.MICROMOLES_PER_LITER
        CURATION_MAP["micromol/l"] = LabUnit.MICROMOLES_PER_LITER
        CURATION_MAP["E/ml"] = LabUnit.UNITS_PER_MILLILITER

        // L/L is an implied unit used for hematocrit
        CURATION_MAP["L/L"] = LabUnit.NONE
        CURATION_MAP["mol/mol"] = LabUnit.NONE
        CURATION_MAP["Ratio"] = LabUnit.NONE
    }

    fun resolve(unit: String): LabUnit {
        if (CURATION_MAP.containsKey(unit)) {
            return CURATION_MAP[unit]!!
        }
        for (labUnit in LabUnit.values()) {
            if (labUnit.display().equals(unit, ignoreCase = true)) {
                return labUnit
            }
        }
        throw IllegalStateException("Could not map lab unit: '$unit'")
    }
}