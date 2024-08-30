package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.Complication

data class ComplicationConfig(
    override val input: String,
    override val ignore: Boolean,
    val impliesUnknownComplicationState: Boolean?,
    val curated: Complication?
) : CurationConfig