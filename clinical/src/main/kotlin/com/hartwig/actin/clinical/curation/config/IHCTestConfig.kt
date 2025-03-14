package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.PriorIHCTest
data class IHCTestConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val curated: PriorIHCTest? = null
) : CurationConfig