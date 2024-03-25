package com.hartwig.actin.clinical.datamodel

import java.time.LocalDate

data class PriorMolecularTest(
    val test: String,
    val item: String,
    val measure: String? = null,
    val measureDate: LocalDate? = null,
    val scoreText: String? = null,
    val scoreValuePrefix: String? = null,
    val scoreValue: Double? = null,
    val scoreValueUnit: String? = null,
    val impliesPotentialIndeterminateStatus: Boolean
)
