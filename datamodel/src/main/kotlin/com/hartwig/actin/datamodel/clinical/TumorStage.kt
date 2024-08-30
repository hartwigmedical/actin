package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

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
