package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.Displayable

enum class TumorStage(val category: TumorStage?) : Displayable {
    I(null),
    II(null),
    IIA(II),
    IIB(II),
    III(null),
    IIIA(III),
    IIIB(III),
    IIIC(III),
    IV(null);

    override fun display(): String {
        return this.toString()
    }
}
