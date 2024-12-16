package com.hartwig.actin.datamodel.clinical

data class Complication(
    val name: String,
    override val icdCode: IcdCode,
    val year: Int?,
    val month: Int?
): IcdCodeHolder
