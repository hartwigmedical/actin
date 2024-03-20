package com.hartwig.actin.clinical.curation.config

data class IntoleranceConfig(
    override val input: String, override val ignore: Boolean = false, val name: String, val doids: Set<String>, val drugAllergyType: String
) : CurationConfig