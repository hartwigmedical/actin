package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.Displayable

enum class ExperimentType(private val display: String) : Displayable {
    HARTWIG_WHOLE_GENOME("Hartwig WGS"),
    HARTWIG_TARGETED("Hartwig Panel"),
    PANEL("NGS Panel");

    override fun display(): String {
        return display
    }

    override fun toString(): String {
        return display
    }
}