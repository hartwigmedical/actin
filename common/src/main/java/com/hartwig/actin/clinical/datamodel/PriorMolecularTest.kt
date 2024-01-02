package com.hartwig.actin.clinical.datamodel

data class PriorMolecularTest(
    val test: String,
    val item: String,
    val measure: String? = null,
    val scoreText: String? = null,
    val scoreValuePrefix: String? = null,
    val scoreValue: Double? = null,
    val scoreValueUnit: String? = null,
    val impliesPotentialIndeterminateStatus: Boolean
)
