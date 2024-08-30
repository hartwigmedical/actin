package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.CypInteraction

data class CypInteractionConfig(override val input: String, override val ignore: Boolean, val interactions: List<CypInteraction>) :
    CurationConfig
