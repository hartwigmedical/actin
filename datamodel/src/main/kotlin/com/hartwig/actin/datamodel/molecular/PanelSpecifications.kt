package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.clinical.SequencingTest
import java.time.LocalDate
import java.util.function.Predicate

data class PanelGeneSpecification(val geneName: String, val targets: List<MolecularTestTarget>)
data class PanelTestSpecification(
    val testName: String,
    val version: LocalDate? = null,
    val configurableForLabs: Set<ExternalLab> = setOf(ExternalLab.ANY)
) {

    override fun toString(): String = "$testName${version?.let { " version $it" } ?: ""}"
}

data class PanelSpecification(private val geneTargetMap: Map<String, List<MolecularTestTarget>>) {
    fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>) =
        geneTargetMap[gene]?.let { molecularTestTargets.test(it) } ?: false
}

fun derivedGeneTargetMap(testResults: SequencingTest) =
    testResults.variants.associate { it.gene to listOf(MolecularTestTarget.MUTATION) } +
            testResults.fusions.flatMap { listOfNotNull(it.geneUp, it.geneDown) }.associateWith { listOf(MolecularTestTarget.FUSION) } +
            testResults.amplifications.map { it.gene to listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.AMPLIFICATION) } +
            testResults.deletions.map { it.gene to listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.DELETION) } +
            testResults.skippedExons.map {
                it.gene to listOf(
                    MolecularTestTarget.FUSION,
                    MolecularTestTarget.MUTATION
                )
            } +
            testResults.negativeResults.map { it.gene to listOf(MolecularTestTarget.MUTATION) }

class PanelSpecifications(panelSpecifications: Map<PanelTestSpecification, List<PanelGeneSpecification>>) {

    private val panelSpecifications: Map<PanelTestSpecification, PanelSpecification> = panelSpecifications.mapValues { (_, geneSpecs) ->
        PanelSpecification(
            geneSpecs.groupBy(PanelGeneSpecification::geneName).mapValues { it.value.flatMap(PanelGeneSpecification::targets) })
    }

    fun panelSpecification(spec: PanelTestSpecification): PanelSpecification {
        return panelSpecifications[spec] ?: throw IllegalStateException(
            "Panel [${spec.testName}${spec.version?.let { " version $it" } ?: ""}] is not found in panel specifications. Check curation and map to one " +
                    "of [${panelSpecifications.keys.joinToString()}] or add this panel to the specification TSV."
        )
    }
}