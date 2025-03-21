package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType

data class FusionMatchCriteria(
    val isReportable: Boolean,
    val geneStart: String,
    val geneEnd: String,
    val fusedExonUp: Int? = null,
    val fusedExonDown: Int? = null,
    val driverType: FusionDriverType
)
