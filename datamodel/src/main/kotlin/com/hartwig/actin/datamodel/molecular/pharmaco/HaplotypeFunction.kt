package com.hartwig.actin.datamodel.molecular.pharmaco

import com.hartwig.actin.datamodel.Displayable

enum class HaplotypeFunction(private val display: String) : Displayable {
    NORMAL_FUNCTION("Normal function"),
    REDUCED_FUNCTION("Reduced function"),
    NO_FUNCTION("No function");

    override fun display(): String {
        return display
    }
}