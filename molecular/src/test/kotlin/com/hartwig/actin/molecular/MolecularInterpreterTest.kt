package com.hartwig.actin.molecular

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularInterpreterTest {
    private val archerPanel = ArcherPanel()
    var annotatorCalled: Boolean = false

    private val extractor = object : MolecularExtractor<PriorMolecularTest, ArcherPanel> {
        override fun extract(input: List<PriorMolecularTest>): List<ArcherPanel> {
            return input.map { archerPanel }
        }
    }
    private val annotator = object : MolecularAnnotator<ArcherPanel> {
        override fun annotate(input: ArcherPanel): ArcherPanel {
            annotatorCalled = true
            return input
        }
    }

    @Test
    fun `Should extract and annotate inputs`() {
        val result = MolecularInterpreter(
            extractor = extractor,
            annotator = annotator,
            inputPredicate = { true }
        ).run(listOf(PriorMolecularTest(test = "test", impliesPotentialIndeterminateStatus = false)))
        assertThat(annotatorCalled).isTrue()
        assertThat(result).containsExactly(archerPanel)
    }

    @Test
    fun `Should filter inputs with predicate`() {
        val result = MolecularInterpreter(
            extractor = extractor,
            annotator = annotator,
            inputPredicate = { false }
        ).run(listOf(PriorMolecularTest(test = "test", impliesPotentialIndeterminateStatus = false)))
        assertThat(annotatorCalled).isFalse()
        assertThat(result).isEmpty()
    }

}