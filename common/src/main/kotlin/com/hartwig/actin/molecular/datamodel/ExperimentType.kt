package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.Displayable

enum class ExperimentType(private val display: String) : Displayable {
    TARGETED("Panel analysis"),
    WHOLE_GENOME("WGS"),
    IHC("IHC"),
    ARCHER("Archer"),
    OTHER("Other");

    override fun display(): String {
        return display
    }
}