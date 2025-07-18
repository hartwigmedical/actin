package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.panel.PanelTestSpecification

class PanelSpecifications(panelSpecifications: Map<PanelTestSpecification, List<PanelGeneSpecification>>) {

    private val molecularTargetsPerTest: Map<PanelTestSpecification, PanelTargetSpecification> = panelSpecifications.mapValues { (_, geneSpecs) ->
        PanelTargetSpecification(
            geneSpecs.groupBy(PanelGeneSpecification::geneName).mapValues { it.value.flatMap(PanelGeneSpecification::targets) })
    }

    val panelTestSpecifications: Set<PanelTestSpecification>
        get() = molecularTargetsPerTest.keys

    fun panelTargetSpecification(testSpec: PanelTestSpecification): PanelTargetSpecification {
        return molecularTargetsPerTest[testSpec] ?: throw IllegalStateException(
            "Panel [${testSpec.testName}${testSpec.versionDate?.let { " version $it" } ?: ""}] is not found in panel specifications. Check curation and map to one " +
                    "of [${molecularTargetsPerTest.keys.joinToString()}] or add this panel to the specification TSV."
        )
    }
}