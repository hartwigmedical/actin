package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.IcdCode

data class ToxicityConfig(
    override val input: String,
    override val ignore: Boolean,
    val name: String,
    val grade: Int?,
    val icdCodes: Set<IcdCode>
) : CurationConfig