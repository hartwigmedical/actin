package com.hartwig.actin.clinical.curation.config

data class QTProlongingConfig(override val input: String, override val ignore: Boolean, val status: String) : CurationConfig
