package com.hartwig.actin.clinical.curation.config

data class MedicationNameConfig(override val input: String, override val ignore: Boolean, val name: String) : CurationConfig