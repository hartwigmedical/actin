package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

data class Intolerance(
    val name: String,
    override val icdCodes: Set<IcdCode>,
    val type: String? = null,
    val clinicalStatus: String? = null,
    val verificationStatus: String? = null,
    val criticality: String? = null
): IcdCodeEntity, Displayable {

    override fun display(): String {
        return name
    }
}
