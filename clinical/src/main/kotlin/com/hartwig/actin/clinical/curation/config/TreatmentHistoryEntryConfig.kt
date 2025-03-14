package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

data class TreatmentHistoryEntryConfig(
    override val input: String,
    override val ignore: Boolean,
    val curated: TreatmentHistoryEntry? = null
) : CurationConfig