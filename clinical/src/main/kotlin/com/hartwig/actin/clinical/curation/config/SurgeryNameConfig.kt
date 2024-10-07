package com.hartwig.actin.clinical.curation.config

data class SurgeryNameConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val name: String
) : CurationConfig