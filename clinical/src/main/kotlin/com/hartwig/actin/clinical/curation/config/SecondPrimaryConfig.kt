package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary

data class SecondPrimaryConfig(
    override val input: String,
    override val ignore: Boolean,
    val curated: PriorSecondPrimary? = null
) : CurationConfig