package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.Displayable

enum class ExperimentType(private val display: String) : Displayable {
    TARGETED("Hartwig Panel"),
    WHOLE_GENOME("Hartwig WGS"),
    IHC("IHC"),
    ARCHER("Archer"),
    GENERIC_PANEL("NGS Panel"),
    OTHER("Other");

    override fun display(): String {
        return display
    }

    override fun toString(): String {
        return display
    }
}