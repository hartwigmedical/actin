package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.OtherCondition

data class NonOncologicalHistoryConfig(
    override val input: String,
    override val ignore: Boolean,
    val lvef: Double? = null,
    val otherCondition: OtherCondition? = null
) : CurationConfig