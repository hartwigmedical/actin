package com.hartwig.actin.efficacy

import com.hartwig.actin.Displayable

enum class EndPointUnit(private val display: String) : Displayable {
    MONTHS("months"),
    PERCENT("%"),
    YES_OR_NO("");

    override fun display(): String {
        return display
    }
}