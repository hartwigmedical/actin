package com.hartwig.actin.algo.ckb.datamodel

import com.hartwig.actin.Displayable

enum class PrimaryEndPointUnit(private val display: String) : Displayable {
    MONTHS("months"),
    PERCENT("%"),
    YES_OR_NO("");

    override fun display(): String {
        return display
    }
}