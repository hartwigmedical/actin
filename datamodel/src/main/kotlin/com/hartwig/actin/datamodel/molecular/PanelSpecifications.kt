package com.hartwig.actin.datamodel.molecular

import java.util.function.Predicate
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

data class PanelGeneSpecification(val geneName: String, val targets: List<MolecularTestTarget>)

data class PanelSpecification(private val geneTargetMap: Map<String, List<MolecularTestTarget>>) {

    fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>): Boolean {
        LOGGER.info("Searching spec map [$geneTargetMap]")
        return geneTargetMap[gene]?.let { molecularTestTargets.test(it) } ?: false
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(PanelSpecifications::class.java)
    }
}

class PanelSpecifications(panelGeneSpecifications: Map<String, List<PanelGeneSpecification>>) {


    private val panelSpecifications: Map<String, PanelSpecification> = panelGeneSpecifications.mapValues { (_, specs) ->
        PanelSpecification(
            specs.groupBy(PanelGeneSpecification::geneName).mapValues { it.value.flatMap(PanelGeneSpecification::targets) })
    }

    fun genesForPanel(panelName: String): PanelSpecification {
        return panelSpecifications[panelName] ?: throw IllegalStateException(
            ("Panel [$panelName] is not found in panel specifications. Check curation and map to one " +
                    "of [${panelSpecifications.keys.joinToString()}] or add this panel to the specification TSV.")
        )
    }
}