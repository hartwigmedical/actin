package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.DrugInteraction

data class DrugInteractionConfig(
    override val input: String,
    override val ignore: Boolean,
    val cypInteractions: List<DrugInteraction>,
    val transporterInteractions: List<DrugInteraction>
) :
    CurationConfig
