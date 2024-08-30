package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition

data class NonOncologicalHistoryConfig(
    override val input: String,
    override val ignore: Boolean,
    val lvef: Double? = null,
    val priorOtherCondition: PriorOtherCondition? = null
) : CurationConfig