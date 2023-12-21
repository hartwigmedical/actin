package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.Displayable

enum class TumorStage(private val category: TumorStage?) : Displayable {
    I(null),
    II(null),
    IIA(II),
    IIB(II),
    III(null),
    IIIA(III),
    IIIB(III),
    IIIC(III),
    IV(null);

    fun category(): TumorStage? {
        return category
    }

    override fun display(): String {
        return this.toString()
    }
}
