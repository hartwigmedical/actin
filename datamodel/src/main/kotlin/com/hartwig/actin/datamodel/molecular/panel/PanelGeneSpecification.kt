package com.hartwig.actin.datamodel.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget

data class PanelGeneSpecification(val geneName: String, val targets: List<MolecularTestTarget>)
