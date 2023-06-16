package com.hartwig.actin.clinical.curation.config

data class MedicationCategoryConfig(
    override val input: String, override val ignore: Boolean = false, val categories: Set<String>
) : CurationConfig