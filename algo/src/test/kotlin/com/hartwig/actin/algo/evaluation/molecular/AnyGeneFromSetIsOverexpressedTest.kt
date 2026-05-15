package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.MolecularEvent
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val PASS_INCLUSION_EVENT = "pass amplification event"
private const val WARN_INCLUSION_EVENT = "warn amplification event"

class AnyGeneFromSetIsOverexpressedTest {

    private val alwaysPassGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>(), any<List<IhcTest>>()) } returns EvaluationFactory.pass(
            "",
            inclusionEvents = setOf(PASS_INCLUSION_EVENT)
        )
    }
    private val alwaysWarnGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>(), any<List<IhcTest>>()) } returns EvaluationFactory.warn(
            "",
            inclusionEvents = setOf(WARN_INCLUSION_EVENT)
        )
    }
    private val alwaysFailGeneAmplificationEvaluation = mockk<GeneIsAmplified> {
        every { evaluate(any<MolecularTest>(), any<List<IhcTest>>()) } returns EvaluationFactory.fail("")
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
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(
            setOf(
                MolecularEvent(PASS_INCLUSION_EVENT, "Potential geneA overexpression"),
                MolecularEvent(WARN_INCLUSION_EVENT, "Potential geneC overexpression")
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined with clarification of DNA result when no amplification present in DNA`() {
        val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { _ -> alwaysFailGeneAmplificationEvaluation }
        val evaluation =
            createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).contains(
            "Overexpression of geneA, geneB and geneC in RNA undetermined (but no amplifications found in DNA)"
        )
        assertThat(evaluation.inclusionMolecularEvents).isEmpty()
    }

    @Test
    fun `Should evaluate to undetermined without clarification of DNA result when genes not tested in DNA`() {
        val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { _ -> alwaysFailGeneAmplificationEvaluation }
        val panelRecord = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularTests = listOf(TestMolecularFactory.createProperPanelTest()))
        val evaluation = createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(panelRecord)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).contains("Overexpression of geneA, geneB and geneC in RNA undetermined")
        assertThat(evaluation.inclusionMolecularEvents).isEmpty()
    }

    @Test
    fun `Should evaluate to undetermined with partial DNA clarification when only some genes tested in DNA panel`() {
        val geneIsAmplifiedCreator: (String) -> GeneIsAmplified = { _ -> alwaysFailGeneAmplificationEvaluation }
        val partialPanelRecord = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularTests = listOf(
                TestMolecularFactory.createMinimalPanelTest().copy(
                    targetSpecification = TestMolecularFactory.panelSpecifications(
                        setOf("geneA", "geneB"),
                        listOf(MolecularTestTarget.AMPLIFICATION)
                    )
                )
            )
        )
        val evaluation = createFunctionWithEvaluations(geneIsAmplifiedCreator).evaluate(partialPanelRecord)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).contains(
            "Overexpression of geneA, geneB and geneC in RNA undetermined (no amplification in DNA for geneA and geneB)"
        )
        assertThat(evaluation.inclusionMolecularEvents).isEmpty()
    }

    private fun createFunctionWithEvaluations(geneIsAmplified: (String) -> GeneIsAmplified): AnyGeneFromSetIsOverexpressed {
        return AnyGeneFromSetIsOverexpressed(setOf("geneA", "geneB", "geneC"), geneIsAmplified)
    }
}