package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTest
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AnyGeneFromSetIsOverexpressedTest {
    private val alwaysPassGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>()) } returns EvaluationFactory.pass("amplification")
    }
    private val alwaysWarnGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>()) } returns EvaluationFactory.warn("possible amplification")
    }
    private val alwaysFailGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>()) } returns EvaluationFactory.fail("no amplification")
    }

    @Test
    fun `Should warn when amplification`() {
        val geneIsAmplifiedCreator: (String, LocalDate?) -> GeneIsAmplified = { gene, _ ->
            when (gene) {
                "gene a" -> alwaysPassGeneAmplificationEvaluation
                "gene b" -> alwaysFailGeneAmplificationEvaluation
                else -> alwaysWarnGeneAmplificationEvaluation
            }
        }
        val evaluation =
            createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).contains("gene a and gene c is amplified therefore possible overexpression in RNA")
    }

    @Test
    fun `Should evaluate to undetermined when no amplification`() {
        val geneIsAmplifiedCreator: (String, LocalDate?) -> GeneIsAmplified = { _, _ -> alwaysFailGeneAmplificationEvaluation }
        val evaluation =
            createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages)
            .contains("Overexpression of gene a, gene b and gene c in RNA undetermined")
    }

    private fun createFunctionWithEvaluations(geneIsAmplified: (String, LocalDate?) -> GeneIsAmplified): AnyGeneFromSetIsOverexpressed {
        return AnyGeneFromSetIsOverexpressed(
            LocalDate.of(2024, 11, 6),
            setOf("gene a", "gene b", "gene c"),
            geneIsAmplified
        )
    }
}