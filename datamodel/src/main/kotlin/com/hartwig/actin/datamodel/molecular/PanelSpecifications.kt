package com.hartwig.actin.datamodel.molecular

data class PanelGeneSpecification(val geneName: String, val targets: List<MolecularTestTarget>)

class PanelSpecifications(private val panelSpecifications: Map<String, List<PanelGeneSpecification>>) {

    fun genesForPanel(panelName: String): Map<String, List<MolecularTestTarget>> {
        return panelSpecifications[panelName]?.groupBy(PanelGeneSpecification::geneName)
            ?.mapValues { it.value.flatMap { c -> c.targets } }
            ?: throw IllegalStateException(
                ("Panel [$panelName] is not found in panel specifications. Check curation and map to one " +
                        "of [${panelSpecifications.keys.joinToString()}] or add this panel to the specification TSV.")
            )
    }
}