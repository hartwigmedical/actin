package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

const val IHC_TEST_TYPE = "IHC"

data class PriorIHCTest(
    val test: String = IHC_TEST_TYPE,
    val item: String? = null,
    val measure: String? = null,
    val measureDate: LocalDate? = null,
    val scoreText: String? = null,
    val scoreValuePrefix: String? = null,
    val scoreValue: Double? = null,
    val scoreValueUnit: String? = null,
    val impliesPotentialIndeterminateStatus: Boolean = false
)
