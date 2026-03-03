package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AnyGeneFromSetIsOverexpressedTest {
    
    private val alwaysPassGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>(), any<List<IhcTest>>()) } returns EvaluationFactory.pass("amplification")
    }
    private val alwaysWarnGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>(), any<List<IhcTest>>()) } returns EvaluationFactory.warn("possible amplification")
    }
    private val alwaysFailGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>(), any<List<IhcTest>>()) } returns EvaluationFactory.fail("no amplification")
    }

    @Test
    fun `Should warn when amplification`() {
        val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { gene ->
            when (gene) {
                "geneA" -> alwaysPassGeneAmplificationEvaluation
                "geneB" -> alwaysFailGeneAmplificationEvaluation
                else -> alwaysWarnGeneAmplificationEvaluation
            }
        }
        val evaluation =
            createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).contains("(Possible) amplification of geneA and geneC detected and therefore possible overexpression in RNA")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential geneA overexpression", "Potential geneC overexpression"))
    }

    @Test
    fun `Should evaluate to undetermined when no amplification`() {
        val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { _ -> alwaysFailGeneAmplificationEvaluation }
        val evaluation =
            createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).contains("Overexpression of geneA, geneB and geneC in RNA undetermined")
        assertThat(evaluation.inclusionMolecularEvents).isEmpty()
    }

    private fun createFunctionWithEvaluations(geneIsAmplified: (String) -> GeneIsAmplified): AnyGeneFromSetIsOverexpressed {
        return AnyGeneFromSetIsOverexpressed(setOf("geneA", "geneB", "geneC"), geneIsAmplified)
    }
}