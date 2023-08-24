package com.hartwig.actin.report.interpretation

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

data class PriorMolecularTestInterpretation(
    val textBasedPriorTests: Map<PriorMolecularTestKey, List<PriorMolecularTest>>,
    val valueBasedPriorTests: Set<PriorMolecularTest>
)