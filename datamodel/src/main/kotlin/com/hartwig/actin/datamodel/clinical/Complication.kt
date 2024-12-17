package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

data class Complication(
    val name: String,
    override val icdCode: IcdCode,
    val year: Int?,
    val month: Int?
): IcdCodeHolder, Displayable {

    override fun display(): String {
        return name
    }
}