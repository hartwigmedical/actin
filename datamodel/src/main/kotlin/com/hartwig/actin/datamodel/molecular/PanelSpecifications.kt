package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.clinical.SequencingTest
import java.util.function.Predicate

data class PanelGeneSpecification(val geneName: String, val targets: List<MolecularTestTarget>)

interface PanelSpecification {
    fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>): Boolean
}

data class KnownPanelSpecification(private val geneTargetMap: Map<String, List<MolecularTestTarget>>) : PanelSpecification {
    override fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>) =
        geneTargetMap[gene]?.let { molecularTestTargets.test(it) } ?: false
}

data class DerivedPanelSpecification(private val testResults: SequencingTest) : PanelSpecification {
    override fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>): Boolean {
        return with(testResults) {
            variants.any { it.gene == gene }
                    || fusions.any { it.geneUp == gene || it.geneDown == gene }
                    || amplifications.any { it.gene == gene }
                    || deletions.any { it.gene == gene }
                    || skippedExons.any { it.gene == gene }
        }
    }
}

class PanelSpecifications(panelGeneSpecifications: Map<String, List<PanelGeneSpecification>>) {

    private val panelSpecifications: Map<String, PanelSpecification> = panelGeneSpecifications.mapValues { (_, specs) ->
        KnownPanelSpecification(
            specs.groupBy(PanelGeneSpecification::geneName).mapValues { it.value.flatMap(PanelGeneSpecification::targets) })
    }

    fun panelSpecification(panelName: String): PanelSpecification {
        return panelSpecifications[panelName] ?: throw IllegalStateException(
            ("Panel [$panelName] is not found in panel specifications. Check curation and map to one " +
                    "of [${panelSpecifications.keys.joinToString()}] or add this panel to the specification TSV.")
        )
    }
}