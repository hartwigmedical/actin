package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasPositiveHER2ExpressionByIHCTest {
    val function = HasPositiveHER2ExpressionByIHC()

    @Test
    fun `Should evaluate to undetermined when no prior molecular tests available`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList()))
        )
    }

    @Test
    fun `Should evaluate to undetermined when HER2 data is not explicitly positive or negative`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIHCTests(listOf(ihcTest(scoreText = "nonsense"), ihcTest(scoreText = "more nonsense")))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to warn when HER2 data is conflicting`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIHCTests(listOf(ihcTest(scoreText = "positive"), ihcTest(scoreText = "negative")))
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).containsExactly(
            "Conflicting HER2 expression tests by IHC"
        )
    }

    @Test
    fun `Should evaluate to undetermined if positive HER2 data is not reliable`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIHCTests(
                listOf(ihcTest(scoreValue = 3.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true))
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to pass if positive HER2 data`() {
        val evaluationValue = function.evaluate(
            MolecularTestFactory.withIHCTests(
                listOf(
                    ihcTest(scoreValue = 3.0, scoreValueUnit = "+"),
                    ihcTest(scoreValue = 2.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true)
                    )
                )
            )
        assertEvaluation(EvaluationResult.PASS, evaluationValue)
        val evaluationText = function.evaluate(
            MolecularTestFactory.withIHCTests(
                listOf(
                    ihcTest(scoreText = "pos", impliesPotentialIndeterminateStatus = true),
                    ihcTest(scoreText = "positive")
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, evaluationText)
    }

    @Test
    fun `Should evaluate to undetermined if ERBB2 is amplified and no valid IHC result available`() {
        val erbb2Amp = TestCopyNumberFactory.createMinimal().copy(
            isReportable = true,
            gene = "ERBB2",
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 20, 20)
        )
        val evaluation = function.evaluate(
            MolecularTestFactory.withCopyNumberAndPriorIHCTests(erbb2Amp, listOf(ihcTest(scoreValue = 2.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true)))
            )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("HER2 expression not tested by IHC but probably positive since ERBB2 amp present")
    }

    @Test
    fun `Should resolve to undetermined if no positive but borderline result`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIHCTests(
                listOf(
                    ihcTest(scoreValue = 2.0, scoreValueUnit = "+")
                    )
                )
            )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly(
            "HER2 expression by IHC was borderline - additional tests should be considered"
        )
    }

    @Test
    fun `Should fail if no positive result`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIHCTests(listOf(ihcTest(scoreValue = 1.0, scoreValueUnit = "+")))
        )
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValueUnit: String? = null, scoreText: String? = null,
                        impliesPotentialIndeterminateStatus: Boolean = false): PriorIHCTest {
        return PriorIHCTest(
            item = "HER2", scoreValue = scoreValue, scoreValueUnit = scoreValueUnit, scoreText = scoreText,
            impliesPotentialIndeterminateStatus = impliesPotentialIndeterminateStatus
        )
    }
}