package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

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
                "geneA" -> alwaysPassGeneAmplificationEvaluation
                "geneB" -> alwaysFailGeneAmplificationEvaluation
                else -> alwaysWarnGeneAmplificationEvaluation
            }
        }
        val evaluation =
            createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).contains("Amplification of geneA and geneC detected and therefore possible overexpression in RNA")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential geneA over exp", "Potential geneC over exp"))
    }

    @Test
    fun `Should evaluate to undetermined when no amplification`() {
        val geneIsAmplifiedCreator: (String, LocalDate?) -> GeneIsAmplified = { _, _ -> alwaysFailGeneAmplificationEvaluation }
        val evaluation =
            createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).contains("Overexpression of geneA, geneB and geneC in RNA undetermined")
        assertThat(evaluation.inclusionMolecularEvents).isEmpty()
    }

    private fun createFunctionWithEvaluations(geneIsAmplified: (String, LocalDate?) -> GeneIsAmplified): AnyGeneFromSetIsOverexpressed {
        return AnyGeneFromSetIsOverexpressed(
            LocalDate.of(2024, 11, 6),
            setOf("geneA", "geneB", "geneC"),
            geneIsAmplified
        )
    }
}