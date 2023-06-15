package com.hartwig.actin.clinical.curation.config

data class MedicationDosageConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val dosageMin: Double?,
    val dosageMax: Double?,
    val dosageUnit: String?,
    val frequency: Double?,
    val frequencyUnit: String?,
    val ifNeeded: Boolean?
) : CurationConfig
