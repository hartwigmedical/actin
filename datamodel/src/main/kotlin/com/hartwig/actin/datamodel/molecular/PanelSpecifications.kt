package com.hartwig.actin.datamodel.molecular

data class PanelGeneSpecification(val geneName: String, val targets: List<MolecularTestTarget>)

class PanelSpecifications(private val panelSpecifications: Map<String, List<PanelGeneSpecification>>) {

    fun genesForPanel(panelName: String): Map<String, List<MolecularTestTarget>> {
        return panelSpecifications[panelName]?.groupBy(PanelGeneSpecification::geneName)
            ?.mapValues { it.value.flatMap { c -> c.targets } } ?: emptyMap()
    }
}