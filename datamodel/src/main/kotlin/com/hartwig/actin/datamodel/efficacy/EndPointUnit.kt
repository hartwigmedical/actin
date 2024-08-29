package com.hartwig.actin.datamodel.efficacy

import com.hartwig.actin.datamodel.Displayable

enum class EndPointUnit(private val display: String) : Displayable {
    MONTHS("months"),
    PERCENT("%"),
    YES_OR_NO("");

    override fun display(): String {
        return display
    }
}