package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.Displayable

enum class ExperimentType(private val display: String) : Displayable {
    WHOLE_GENOME("Hartwig WGS"),
    TARGETED("Hartwig Panel"),
    ARCHER("Archer"),
    GENERIC_PANEL("NGS Panel"),
    IHC("IHC"),
    OTHER("Other"),
    CDX("CDx");

    override fun display(): String {
        return display
    }

    override fun toString(): String {
        return display
    }
}