package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.Displayable

enum class ExperimentType(private val display: String) : Displayable {
    TARGETED("Panel analysis"),
    WHOLE_GENOME("WGS"),
    AMPLISEQ("AmpliSeq panel");

    override fun display(): String {
        return display
    }
}