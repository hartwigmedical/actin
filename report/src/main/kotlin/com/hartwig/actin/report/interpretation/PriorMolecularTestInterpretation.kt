package com.hartwig.actin.report.interpretation

import com.google.common.collect.Multimap
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

data class PriorMolecularTestInterpretation(
    val textBasedPriorTests: Multimap<PriorMolecularTestKey, PriorMolecularTest>,
    val valueBasedPriorTests: Set<PriorMolecularTest>,
)