package com.hartwig.actin.datamodel.clinical

//TODO(make categories property redundant)
data class Complication(
    val name: String,
    val categories: Set<String>,
    override val icdCode: IcdCode,
    val year: Int?,
    val month: Int?
): IcdCodeHolder
