package com.hartwig.actin.datamodel.clinical

data class Intolerance(
    val name: String,
    override val icdCode: IcdCode,
    val category: String? = null,
    val subcategories: Set<String>? = null,
    val type: String? = null,
    val clinicalStatus: String? = null,
    val verificationStatus: String? = null,
    val criticality: String? = null
): IcdCodeHolder
