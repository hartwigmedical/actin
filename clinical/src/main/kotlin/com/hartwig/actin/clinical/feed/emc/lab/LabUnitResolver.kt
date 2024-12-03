package com.hartwig.actin.clinical.feed.emc.lab

import com.hartwig.actin.datamodel.clinical.LabUnit

object LabUnitResolver {

    val CURATION_MAP = mapOf(
        "10*6/L" to LabUnit.MILLIONS_PER_LITER,
        "*10E6/l" to LabUnit.MILLIONS_PER_LITER,
        "10*9/L" to LabUnit.BILLIONS_PER_LITER,
        "*10E9/l" to LabUnit.BILLIONS_PER_LITER,
        "10*12/L" to LabUnit.TRILLIONS_PER_LITER,
        "% van de leukocyten" to LabUnit.PERCENTAGE_OF_LEUKOCYTES,
        "% van de T-cellen" to LabUnit.PERCENTAGE_OF_T_CELLS,
        "mmol/mol Kreatinine" to LabUnit.MILLIMOLES_PER_MOLE,
        "g/mol Kreatinine" to LabUnit.GRAMS_PER_MOLE,
        "µg/L" to LabUnit.MICROGRAMS_PER_LITER,
        "µmol/L" to LabUnit.MICROMOLES_PER_LITER,
        "micromol/l" to LabUnit.MICROMOLES_PER_LITER,
        "E/ml" to LabUnit.UNITS_PER_MILLILITER,
        // L/L is an implied unit used for hematocrit
        "L/L" to LabUnit.NONE,
        "mol/mol" to LabUnit.NONE,
        "Ratio" to LabUnit.NONE
    )

    fun resolve(unit: String) = CURATION_MAP[unit]
        ?: LabUnit.entries.firstOrNull { it.display().equals(unit, ignoreCase = true) }
        ?: throw IllegalStateException("Could not map lab unit: '$unit'")
}