package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
data class MolecularTestConfig(
    override val input: String,
    override val ignore: Boolean,
    val curated: PriorMolecularTest?
) : CurationConfig