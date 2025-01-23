package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.Comorbidity

data class ComorbidityConfig(
    override val input: String,
    override val ignore: Boolean,
    val lvef: Double? = null,
    val curated: Comorbidity? = null
) : CurationConfig
