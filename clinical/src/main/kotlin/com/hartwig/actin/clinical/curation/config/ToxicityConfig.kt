package com.hartwig.actin.clinical.curation.config

data class ToxicityConfig(
    override val input: String,
    override val ignore: Boolean,
    val name: String,
    val categories: Set<String>,
    val grade: Int?,
    val icdCode: String
) : CurationConfig