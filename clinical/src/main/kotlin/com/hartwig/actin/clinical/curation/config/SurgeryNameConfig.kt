package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.SurgeryType

data class SurgeryNameConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val name: String,
    val type: SurgeryType? = null,
) : CurationConfig