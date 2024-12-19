package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.IcdCode

data class IntoleranceConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val name: String,
    val icd: Set<IcdCode>,
) : CurationConfig