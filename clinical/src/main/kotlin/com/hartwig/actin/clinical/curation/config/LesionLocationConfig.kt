package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory

data class LesionLocationConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val location: String,
    val category: LesionLocationCategory? = null,
    val active: Boolean? = null
) : CurationConfig