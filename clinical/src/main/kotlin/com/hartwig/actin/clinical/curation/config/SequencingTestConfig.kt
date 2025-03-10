package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult

data class SequencingTestConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val curated: ProvidedMolecularTestResult? = null
) : CurationConfig