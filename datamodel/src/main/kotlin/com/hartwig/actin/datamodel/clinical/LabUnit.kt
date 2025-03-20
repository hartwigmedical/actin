package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class LabUnit(private val display: String) : Displayable {
    BILLIONS_PER_LITER("10^9/L"),
    CELLS_PER_CUBIC_MILLIMETER("cells/mm3"),
    CELLS_PER_MICROLITER("cells/µL"),
    FEMTOLITERS("fL"),
    FEMTOMOLES("fmol"),
    GRAMS_PER_DAY("g/24h"),
    GRAMS_PER_DECILITER("g/dL"),
    GRAMS_PER_LITER("g/L"),
    GRAMS_PER_MOLE("g/mol"),
    GRAMS("g"),
    INTERNATIONAL_UNITS_PER_LITER("IU/L"),
    INTERNATIONAL_UNITS_PER_MILLILITER("IU/ml"),
    KILO_PASCAL("kPa"),
    KILOGRAMS_PER_LITER("kg/L"),
    KILOUNITS_PER_LITER("kU/L"),
    MICROGRAMS_PER_GRAM("µg/g"),
    MICROGRAMS_PER_LITER("ug/L"),
    MICROGRAMS_PER_MICROLITER("µg/µL"),
    MICROMOLES_PER_LITER("umol/L"),
    MILLI_OSMOLE_PER_KILOGRAM("mOsm/kg"),
    MILLIGRAMS_PER_DECILITER("mg/dL"),
    MILLIGRAMS_PER_GRAM("mg/g"),
    MILLIGRAMS_PER_LITER("mg/L"),
    MILLIGRAMS_PER_MILLIMOLE("mg/mmol"),
    MILLILITERS_PER_MINUTE("mL/min"),
    MILLILITERS("mL"),
    MILLIMETERS_PER_HOUR("mm/hr"),
    MILLIMOLES_PER_DAY("mmol/24h"),
    MILLIMOLES_PER_LITER("mmol/L"),
    MILLIMOLES_PER_MOLE("mmol/mol"),
    MILLIONS_PER_LITER("10^6/L"),
    MILLIONS_PER_MILLILITER("10^6/mL"),
    MILLIUNITS_PER_LITER("mU/L"),
    NANOGRAMS_PER_DECILITER("ng/dL"),
    NANOGRAMS_PER_LITER("ng/L"),
    NANOGRAMS_PER_MILLILITER("ng/mL"),
    NANOMOLES_PER_DAY("nmol/24h"),
    NANOMOLES_PER_LITER("nmol/L"),
    PERCENTAGE_OF_LEUKOCYTES("% of leukocytes"),
    PERCENTAGE_OF_T_CELLS("% of T-cells"),
    PERCENTAGE("%"),
    PICOMOLES_PER_LITER("pmol/L"),
    PRNT50("PRNT50"),
    SECONDS("sec"),
    TRILLIONS_PER_LITER("10^12/L"),
    UNITS_OF_INR("INR"),
    UNITS_PER_LITER("U/L"),
    UNITS_PER_MILLILITER("U/mL"),
    NONE("");

    override fun display(): String {
        return display
    }
}
