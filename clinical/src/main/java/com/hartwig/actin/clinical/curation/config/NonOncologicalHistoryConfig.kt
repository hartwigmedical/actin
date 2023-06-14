package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import java.util.Optional

data class NonOncologicalHistoryConfig(
    override val input: String,
    override val ignore: Boolean,
    val lvef: Optional<Double>,
    val priorOtherCondition: Optional<PriorOtherCondition>
) : CurationConfig