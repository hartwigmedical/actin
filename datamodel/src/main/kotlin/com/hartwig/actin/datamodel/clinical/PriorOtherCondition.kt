package com.hartwig.actin.datamodel.clinical

data class PriorOtherCondition(
    val name: String,
    val year: Int? = null,
    val month: Int? = null,
    override val icdCode: IcdCode,
    val isContraindicationForTherapy: Boolean
): IcdCodeHolder
