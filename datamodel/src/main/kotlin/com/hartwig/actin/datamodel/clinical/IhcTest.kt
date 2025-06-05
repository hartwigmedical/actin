package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class IhcTest(
    val item: String,
    val measure: String? = null,
    val measureDate: LocalDate? = null,
    val scoreText: String? = null,
    val scoreValuePrefix: String? = null,
    val scoreValue: Double? = null,
    val scoreValueUnit: String? = null,
    val impliesPotentialIndeterminateStatus: Boolean = false,
    val reportHash: String? = null
)
