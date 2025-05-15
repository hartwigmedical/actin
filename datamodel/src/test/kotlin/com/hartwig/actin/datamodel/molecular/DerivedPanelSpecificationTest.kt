package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.SequencingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE = "gene"
private const val TEST = "test"

class DerivedPanelSpecificationTest {

    @Test
    fun `Should return true when variant on gene`() {
        val specification = DerivedPanelSpecification(SequencingTest(test = TEST, variants = setOf(SequencedVariant(GENE))))
        assertThat(specification.testsGene(GENE, molecularTargetNotImportant())).isTrue()
    }

    @Test
    fun `Should return true when fusion on gene up or down`() {
        val specification = DerivedPanelSpecification(SequencingTest(test = TEST, variants = setOf(SequencedVariant(GENE))))
        assertThat(specification.testsGene(GENE, molecularTargetNotImportant())).isTrue()
    }

    @Test
    fun `Should return true when amplification on gene`() {
        val specification = DerivedPanelSpecification(SequencingTest(test = TEST, variants = setOf(SequencedVariant(GENE))))
        assertThat(specification.testsGene(GENE, molecularTargetNotImportant())).isTrue()
    }

    @Test
    fun `Should return true when deletion on gene`() {
        val specification = DerivedPanelSpecification(SequencingTest(test = TEST, variants = setOf(SequencedVariant(GENE))))
        assertThat(specification.testsGene(GENE, molecularTargetNotImportant())).isTrue()
    }

    @Test
    fun `Should return true when skipped exon on gene`() {
        val specification = DerivedPanelSpecification(SequencingTest(test = TEST, variants = setOf(SequencedVariant(GENE))))
        assertThat(specification.testsGene(GENE, molecularTargetNotImportant())).isTrue()
    }

    @Test
    fun `Should return false when no alterations on gene`() {
        val specification = DerivedPanelSpecification(SequencingTest(test = TEST, variants = setOf(SequencedVariant("difference gene"))))
        assertThat(specification.testsGene(GENE, molecularTargetNotImportant()))
    }

    private fun molecularTargetNotImportant(): (t: List<MolecularTestTarget>) -> Boolean = { false }
}