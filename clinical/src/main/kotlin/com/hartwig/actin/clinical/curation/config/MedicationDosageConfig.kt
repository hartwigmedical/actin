package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.Dosage

data class MedicationDosageConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val curated: Dosage
) : CurationConfig
