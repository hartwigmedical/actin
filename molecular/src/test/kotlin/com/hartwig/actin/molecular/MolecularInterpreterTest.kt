package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularInterpreterTest {
    private val output = mockk<MolecularTest>()
    var annotatorCalled: Boolean = false

    private val extractor = object : MolecularExtractor<PriorSequencingTest, PriorSequencingTest> {
        override fun extract(input: List<PriorSequencingTest>): List<PriorSequencingTest> {
            return input
        }
    }
    private val annotator = object : MolecularAnnotator<PriorSequencingTest, MolecularTest> {
        override fun annotate(input: PriorSequencingTest): MolecularTest {
            annotatorCalled = true
            return output
        }
    }

    @Test
    fun `Should extract and annotate inputs`() {
        val result = MolecularInterpreter(
            extractor = extractor,
            annotator = annotator
        ).run(listOf(PriorSequencingTest(test = "test")))
        assertThat(annotatorCalled).isTrue()
        assertThat(result).containsExactly(output)
    }
}