package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

data class PriorOtherCondition(
    val name: String,
    val year: Int? = null,
    val month: Int? = null,
    override val icdCodes: Set<IcdCode>,
    val isContraindicationForTherapy: Boolean
): IcdCodeEntity, Displayable {

    override fun display(): String {
        return name
    }
}