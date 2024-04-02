package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.Displayable

enum class ExperimentType(private val display: String) : Displayable {
    TARGETED("Panel analysis"),
    WHOLE_GENOME("WGS"),
    IHC("IHC");


    override fun display(): String {
        return display
    }
}