package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedNegativeResult
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.panel.PanelSpecificationFunctions.derivedGeneTargetMap
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.panel.PanelTestSpecification
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

private const val GENE = "gene"
private const val ANOTHER_GENE = "another gene"
private const val TEST = "test"

class PanelSpecificationsTest {

    @Test
    fun `Should evaluate target predicate when gene is in panel specification`() {
        val targets = listOf(MolecularTestTarget.MUTATION)
        val specification = PanelTargetSpecification(mapOf(GENE to targets))
        assertThat(specification.testsGene(GENE) { it == targets }).isTrue()
        assertThat(specification.testsGene(GENE) { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
    }

    @Test
    fun `Should return false if gene is not in specification`() {
        val specification = PanelTargetSpecification(mapOf(GENE to emptyList()))
        assertThat(specification.testsGene(ANOTHER_GENE) { true })
    }

    @Test
    fun `Should resolve a panels specification from the set of all specification by name and negative results`() {
        val panelSpec = PanelTestSpecification("panel", null)
        val negativeResults = setOf(SequencedNegativeResult(ANOTHER_GENE, MolecularTestTarget.FUSION))
        val specification = PanelSpecifications(
            mapOf(
                panelSpec to listOf(
                    PanelGeneSpecification(
                        GENE,
                        listOf(MolecularTestTarget.MUTATION)
                    )
                )
            )
        ).panelTargetSpecification(panelSpec, negativeResults)
        assertThat(specification.testsGene(GENE) { it == listOf(MolecularTestTarget.MUTATION) })
        assertThat(specification.testsGene(ANOTHER_GENE) { it == listOf(MolecularTestTarget.FUSION) })
    }

    @Test
    fun `Should throw illegal state exception when a panel name is not found`() {
        assertThatThrownBy {
            val specifications = PanelSpecifications(emptyMap())
            specifications.panelTargetSpecification(PanelTestSpecification("panel"), null)
        }.isInstanceOfAny(IllegalStateException::class.java)
    }

    @Test
    fun `Should return true when specifications derived from test and mutation is on gene`() {
        val derivedSpecification =
            PanelTargetSpecification(derivedGeneTargetMap(SequencingTest(test = TEST, variants = setOf(SequencedVariant(gene = GENE)))))
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isTrue()
        assertThat(derivedSpecification.testsGene(ANOTHER_GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isFalse()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.FUSION))).isFalse()
    }

    @Test
    fun `Should return true when specifications derived from test and fusion is on gene`() {
        val geneDown = "gene down"
        val derivedSpecification =
            PanelTargetSpecification(
                derivedGeneTargetMap(
                    SequencingTest(
                        test = TEST,
                        fusions = setOf(SequencedFusion(geneUp = GENE, geneDown = geneDown))
                    )
                )
            )
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.FUSION))).isTrue()
        assertThat(derivedSpecification.testsGene(geneDown, predicateForTargets(MolecularTestTarget.FUSION))).isTrue()
        assertThat(derivedSpecification.testsGene(ANOTHER_GENE, predicateForTargets(MolecularTestTarget.FUSION))).isFalse()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isFalse()
    }

    @Test
    fun `Should return true when specifications derived from test and mutation is on amplification`() {
        val derivedSpecification =
            PanelTargetSpecification(
                derivedGeneTargetMap(
                    SequencingTest(
                        test = TEST,
                        amplifications = setOf(SequencedAmplification(gene = GENE))
                    )
                )
            )
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.AMPLIFICATION))).isTrue()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isTrue()
        assertThat(derivedSpecification.testsGene(ANOTHER_GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isFalse()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.FUSION))).isFalse()
    }

    @Test
    fun `Should return true when specifications derived from test and mutation is on deletion`() {
        val derivedSpecification =
            PanelTargetSpecification(derivedGeneTargetMap(SequencingTest(test = TEST, deletions = setOf(SequencedDeletion(gene = GENE)))))
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.DELETION))).isTrue()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isTrue()
        assertThat(derivedSpecification.testsGene(ANOTHER_GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isFalse()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.FUSION))).isFalse()
    }

    @Test
    fun `Should return true when specifications derived from test and mutation is on skipped exons`() {
        val derivedSpecification =
            PanelTargetSpecification(
                derivedGeneTargetMap(
                    SequencingTest(
                        test = TEST,
                        skippedExons = setOf(SequencedSkippedExons(gene = GENE, exonStart = 1, exonEnd = 2))
                    )
                )
            )
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isTrue()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.FUSION))).isTrue()
        assertThat(derivedSpecification.testsGene(ANOTHER_GENE, predicateForTargets(MolecularTestTarget.MUTATION))).isFalse()
        assertThat(derivedSpecification.testsGene(GENE, predicateForTargets(MolecularTestTarget.AMPLIFICATION))).isFalse()
    }

    private fun predicateForTargets(target: MolecularTestTarget): (t: List<MolecularTestTarget>) -> Boolean =
        { it.contains(target) }
}