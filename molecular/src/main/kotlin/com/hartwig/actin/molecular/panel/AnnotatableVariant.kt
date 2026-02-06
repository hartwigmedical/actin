package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.tools.variant.Variant as TransvarVariant

data class AnnotatableVariant(
    val queryId: Int,
    val sequencedVariant: SequencedVariant,
    val queryHgvs: String,
    val localPhaseSet: Int?,
    val transvarVariant: TransvarVariant? = null,
    val paveResponse: PaveResponse? = null,
)