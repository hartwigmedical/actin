package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasPositiveHER2ExpressionByIhcTest {
    val function = HasPositiveHER2ExpressionByIhc()

    @Test
    fun `Should evaluate to undetermined when no prior molecular tests available`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList()))
        )
    }

    @Test
    fun `Should evaluate to undetermined when HER2 data is not explicitly positive or negative`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(listOf(ihcTest(scoreText = "nonsense"), ihcTest(scoreText = "more nonsense")))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to warn when HER2 data is conflicting`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(listOf(ihcTest(scoreText = "positive"), ihcTest(scoreText = "negative")))
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).containsExactly("Conflicting IHC HER2 expression test results")
    }

    @Test
    fun `Should evaluate to undetermined if positive HER2 data is not reliable`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(ihcTest(scoreValue = 3.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true))
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to pass if positive HER2 data`() {
        val evaluationValue = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 3.0, scoreValueUnit = "+"),
                    ihcTest(scoreValue = 2.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true)
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, evaluationValue)
        val evaluationText = function.evaluate(
            MolecularTestFactory.withIhcTests(
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
            MolecularTestFactory.withCopyNumberAndIhcTests(
                erbb2Amp,
                listOf(ihcTest(scoreValue = 2.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true))
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("No IHC HER2 expression test available (but ERBB2 amplification detected)")
    }

    @Test
    fun `Should resolve to undetermined if no positive but borderline result`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 2.0, scoreValueUnit = "+")
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("Undetermined if IHC HER2 score value(s) '2.0' is considered positive")
    }

    @Test
    fun `Should fail if no positive result`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(listOf(ihcTest(scoreValue = 1.0, scoreValueUnit = "+")))
        )
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    private fun ihcTest(
        scoreValue: Double? = null, scoreValueUnit: String? = null, scoreText: String? = null,
        impliesPotentialIndeterminateStatus: Boolean = false
    ): IhcTest {
        return IhcTest(
            item = "HER2", scoreValue = scoreValue, scoreValueUnit = scoreValueUnit, scoreText = scoreText,
            impliesPotentialIndeterminateStatus = impliesPotentialIndeterminateStatus
        )
    }
}