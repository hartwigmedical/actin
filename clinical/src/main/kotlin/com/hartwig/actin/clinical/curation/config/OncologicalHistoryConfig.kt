package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment

data class OncologicalHistoryConfig(
    override val input: String,
    override val ignore: Boolean,
    val curated: PriorTumorTreatment?
) : CurationConfig