package com.hartwig.actin.molecular

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularInterpreterTest {
    private val extraction = ArcherPanelExtraction()
    private val output = mockk<OtherPriorMolecularTest>()
    var annotatorCalled: Boolean = false

    private val extractor = object : MolecularExtractor<PriorMolecularTest, ArcherPanelExtraction> {
        override fun extract(input: List<PriorMolecularTest>): List<ArcherPanelExtraction> {
            return input.map { extraction }
        }
    }
    private val annotator = object : MolecularAnnotator<ArcherPanelExtraction, MolecularTest> {
        override fun annotate(input: ArcherPanelExtraction): OtherPriorMolecularTest {
            annotatorCalled = true
            return output
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
        assertThat(result).containsExactly(output)
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