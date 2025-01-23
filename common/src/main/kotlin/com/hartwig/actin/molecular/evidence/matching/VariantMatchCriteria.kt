package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType

data class VariantMatchCriteria(
    val isReportable: Boolean,
    val gene: String,
    val codingEffect: CodingEffect? = null,
    val type: VariantType? = null,
    val chromosome: String? = null,
    val position: Int? = null,
    val ref: String? = null,
    val alt: String? = null
)
