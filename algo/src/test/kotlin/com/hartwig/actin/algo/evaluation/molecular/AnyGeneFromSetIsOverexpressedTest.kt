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

private val GENES = setOf("gene a", "gene b", "gene c")

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
        val geneIsAmplifiedCreator: (String, LocalDate?) -> GeneIsAmplified = { gene, _ ->
            when (gene) {
                "gene a" -> alwaysPassGeneAmplificationEvaluation
                "gene b" -> alwaysFailGeneAmplificationEvaluation
                else -> alwaysWarnGeneAmplificationEvaluation
            }
        }
        val evaluation = AnyGeneFromSetIsOverexpressed(
            null,
            GENES,
            geneIsAmplifiedCreator
        ).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).contains("gene a and gene c is amplified therefore possible overexpression in RNA")
    }

    @Test
    fun `Should evaluate to undetermined when no amplification`() {
        val geneIsAmplifiedCreator: (String, LocalDate?) -> GeneIsAmplified = { _, _ -> alwaysFailGeneAmplificationEvaluation }
        val evaluation = AnyGeneFromSetIsOverexpressed(
            null,
            GENES,
            geneIsAmplifiedCreator
        ).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages)
            .contains("Overexpression of gene a, gene b and gene c in RNA undetermined")
    }

    @Test
    fun `Should evaluate to undetermined when molecular record not available`() {
        val geneIsAmplifiedCreator: (String, LocalDate?) -> GeneIsAmplified = { _, _ -> alwaysFailGeneAmplificationEvaluation }
        val evaluation = AnyGeneFromSetIsOverexpressed(
            null,
            GENES,
            geneIsAmplifiedCreator
        ).evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("No molecular data to determine overexpression of gene a, gene b and gene c in RNA")
    }
}