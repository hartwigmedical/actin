package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class TumorStage(val category: TumorStage?) : Displayable {
    I(null),
    IA(I),
    IB(I),
    II(null),
    IIA(II),
    IIB(II),
    IIC(II),
    III(null),
    IIIA(III),
    IIIB(III),
    IIIC(III),
    IIID(III),
    IV(null),
    IVA(IV),
    IVB(IV),
    IVC(IV);

    override fun display(): String {
        return this.toString()
    }
}
