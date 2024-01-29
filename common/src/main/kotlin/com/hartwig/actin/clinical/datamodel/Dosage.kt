package com.hartwig.actin.clinical.datamodel

data class Dosage(
    val dosageMin: Double? = null,
    val dosageMax: Double? = null,
    val dosageUnit: String? = null,
    val frequency: Double? = null,
    val frequencyUnit: String? = null,
    val periodBetweenValue: Double? = null,
    val periodBetweenUnit: String? = null,
    val ifNeeded: Boolean? = null
)
