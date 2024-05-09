package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType

data class VariantMatchCriteria(
    val isReportable: Boolean,
    val gene: String,
    val codingEffect: CodingEffect?,
    val type: VariantType,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
)
