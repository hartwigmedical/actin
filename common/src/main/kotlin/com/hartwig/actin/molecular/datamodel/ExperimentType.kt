package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.Displayable

enum class ExperimentType(private val display: String) : Displayable {
    HARTWIG_WHOLE_GENOME("Hartwig WGS"),
    HARTWIG_TARGETED("Hartwig Panel"),
    PANEL("NGS Panel"),
    OTHER("Other");

    override fun display(): String {
        return display
    }

    override fun toString(): String {
        return display
    }
}