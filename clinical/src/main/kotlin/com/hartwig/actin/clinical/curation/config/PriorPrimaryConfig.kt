package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.PriorPrimary

data class PriorPrimaryConfig(
    override val input: String,
    override val ignore: Boolean,
    val curated: PriorPrimary? = null
) : CurationConfig