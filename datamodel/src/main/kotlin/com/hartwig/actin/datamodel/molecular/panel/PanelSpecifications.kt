package com.hartwig.actin.datamodel.molecular.panel

class PanelSpecifications(panelSpecifications: Map<PanelTestSpecification, List<PanelGeneSpecification>>) {

    private val panelSpecifications: Map<PanelTestSpecification, PanelTargetSpecification> = panelSpecifications.mapValues { (_, geneSpecs) ->
        PanelTargetSpecification(
            geneSpecs.groupBy(PanelGeneSpecification::geneName).mapValues { it.value.flatMap(PanelGeneSpecification::targets) })
    }

    val panelTestSpecifications: Set<PanelTestSpecification>
        get() = panelSpecifications.keys

    fun panelTargetSpecification(testSpec: PanelTestSpecification): PanelTargetSpecification {
        return panelSpecifications[testSpec] ?: throw IllegalStateException(
            "Panel [${testSpec.testName}${testSpec.versionDate?.let { " version $it" } ?: ""}] is not found in panel specifications. Check curation and map to one " +
                    "of [${panelSpecifications.keys.joinToString()}] or add this panel to the specification TSV."
        )
    }
}