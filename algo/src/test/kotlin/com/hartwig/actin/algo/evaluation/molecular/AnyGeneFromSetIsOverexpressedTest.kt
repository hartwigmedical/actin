package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AnyGeneFromSetIsOverexpressedTest {
    private val alwaysPassGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularRecord>()) } returns EvaluationFactory.pass("amplification")
    }
    private val alwaysWarnGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularRecord>()) } returns EvaluationFactory.warn("possible amplification")
    }
    private val alwaysFailGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularRecord>()) } returns EvaluationFactory.fail("no amplification")
    }

    @Test
    fun `Should warn when amplification`() {
        val function = createFunctionWithEvaluations(
            mapOf(
                "gene a" to alwaysPassGeneAmplificationEvaluation,
                "gene b" to alwaysFailGeneAmplificationEvaluation,
                "gene c" to alwaysWarnGeneAmplificationEvaluation
            )
        )
        val evaluation = function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).contains("gene a and gene c is amplified therefore possible overexpression of gene(s)")
    }

    @Test
    fun `Should evaluate to undetermined when no amplification`() {
        val function = createFunctionWithEvaluations(
            mapOf(
                "gene a" to alwaysFailGeneAmplificationEvaluation,
                "gene b" to alwaysFailGeneAmplificationEvaluation,
                "gene c" to alwaysFailGeneAmplificationEvaluation
            )
        )
        val evaluation = function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages)
            .contains("Overexpression of gene a, gene b and gene c in RNA undetermined")
    }

    @Test
    fun `Should evaluate to undetermined when molecular record not available`() {
        val function = createFunctionWithEvaluations(
            mapOf(
                "gene a" to alwaysFailGeneAmplificationEvaluation,
                "gene b" to alwaysFailGeneAmplificationEvaluation,
                "gene c" to alwaysFailGeneAmplificationEvaluation
            )
        )
        val evaluation = function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("No molecular data to determine overexpression of gene a, gene b and gene c in RNA")
    }

    private fun createFunctionWithEvaluations(evaluations: Map<String, GeneIsAmplified>): AnyGeneFromSetIsOverexpressed {
        return AnyGeneFromSetIsOverexpressed(
            LocalDate.of(2024, 11, 6),
            evaluations
        )
    }
}