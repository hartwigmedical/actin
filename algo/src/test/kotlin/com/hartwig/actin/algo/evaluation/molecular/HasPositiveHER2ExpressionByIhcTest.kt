package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
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
    fun `Should be undetermined when no prior molecular tests available`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList()))
        )
    }

    @Test
    fun `Should warn if ERBB2 is amplified and no IHC HER2 results`() {
        val erbb2Amp = TestCopyNumberFactory.createMinimal().copy(
            isReportable = true,
            gene = "ERBB2",
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 20, 20)
        )
        val evaluation = function.evaluate(MolecularTestFactory.withCopyNumberAndIhcTests(erbb2Amp, emptyList()))

        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("No IHC HER2 expression test available (but ERBB2 amplification detected)")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 positive"))
        assertThat(evaluation.isMissingMolecularResultForEvaluation).isTrue
    }

    @Test
    fun `Should pass if all positive HER2 data`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 3.0, scoreValueUnit = "+"),
                    ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("IHC HER2 positive"))
    }

    @Test
    fun `Should fail if all negative HER2 data`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 0.0),
                    ihcTest(scoreValue = 1.0, scoreValueUnit = "+"),
                    ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should warn if borderline result`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 2.0, scoreValueUnit = "+")
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if IHC HER2 score value(s) is considered positive")
    }

    @Test
    fun `Should warn when HER2 data is not explicitly positive or negative`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreText = "nonsense"),
                    ihcTest(scoreText = "more nonsense")
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
    }

    @Test
    fun `Should warn when HER2 data is conflicting`() {
        val evaluation =
            function.evaluate(
                MolecularTestFactory.withIhcTests(
                    listOf(
                        ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS.first()),
                        ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first())
                    )
                )
            )
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if HER2 IHC test results indicate positive HER2 status")
    }

    @Test
    fun `Should warn if no certain IHC result available`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(
                        scoreText = IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS.first(),
                        impliesPotentialIndeterminateStatus = true
                    )
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
    }

    @Test
    fun `Should warn if ERBB2 is amplified and no certain IHC result available`() {
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
                listOf(
                    ihcTest(
                        scoreText = IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS.first(),
                        impliesPotentialIndeterminateStatus = true
                    )
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if HER2 IHC test results indicate positive HER2 status (but ERBB2 amplification detected)")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 positive"))
    }

    private fun ihcTest(
        scoreValue: Double? = null,
        scoreValueUnit: String? = null,
        scoreText: String? = null,
        impliesPotentialIndeterminateStatus: Boolean = false
    ): IhcTest {
        return IhcTest(
            item = "HER2",
            scoreValue = scoreValue,
            scoreValueUnit = scoreValueUnit,
            scoreText = scoreText,
            impliesPotentialIndeterminateStatus = impliesPotentialIndeterminateStatus
        )
    }
}