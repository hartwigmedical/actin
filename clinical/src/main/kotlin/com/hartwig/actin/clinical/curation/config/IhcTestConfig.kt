package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.IhcTest

data class IhcTestConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val curated: IhcTest? = null
) : CurationConfig