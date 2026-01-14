package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.IhcTestResult
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val ERBB2_AMP = TestCopyNumberFactory.createMinimal().copy(
    isReportable = true,
    gene = "ERBB2",
    geneRole = GeneRole.ONCO,
    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 20, 20)
)

class HasHER2ExpressionByIhcTest {

    val positiveFunction = HasHER2ExpressionByIhc(IhcTestResult.POSITIVE)
    val negativeFunction = HasHER2ExpressionByIhc(IhcTestResult.NEGATIVE)
    val lowFunction = HasHER2ExpressionByIhc(IhcTestResult.LOW)

    @Test
    fun `Should evaluate to undetermined when no prior molecular tests available`() {
        listOf(positiveFunction, negativeFunction, lowFunction).forEach { function ->
            assertMolecularEvaluation(
                EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList()))
            )
        }
    }

    @Test
    fun `Should evaluate HER2 positive to warn if ERBB2 is amplified and no IHC HER2 results`() {
        val evaluation = positiveFunction.evaluate(MolecularTestFactory.withCopyNumberAndIhcTests(ERBB2_AMP, emptyList()))

        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("No IHC HER2 expression test available (but ERBB2 amplification detected)")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 positive"))
        assertThat(evaluation.isMissingMolecularResultForEvaluation).isTrue
    }

    @Test
    fun `Should pass HER2 positive if only positive HER2 IHC data`() {
        val evaluation = positiveFunction.evaluate(
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
    fun `Should fail HER2 positive if only negative HER2 IHC data`() {
        val evaluation = positiveFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 0.0),
                    ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should fail HER2 positive if only low HER2 IHC data`() {
        val evaluation = positiveFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 1.0, scoreValueUnit = "+"),
                    ihcTest(scoreText = IhcTestEvaluationConstants.LOW_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should evaluate HER2 positive to undetermined if borderline result`() {
        val evaluation = positiveFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 2.0, scoreValueUnit = "+")
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if IHC HER2 score value(s) is considered positive")
        assertThat(evaluation.isMissingMolecularResultForEvaluation).isTrue
    }

    @Test
    fun `Should evaluate HER2 positive to warn when IHC HER2 data is not explicitly positive or negative`() {
        val evaluation = positiveFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreText = "nonsense"),
                    ihcTest(scoreText = "more nonsense")
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if HER2 IHC test results indicate positive HER2 status")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 positive"))
    }

    @Test
    fun `Should evaluate HER2 positive to warn when IHC HER2 data is conflicting`() {
        val evaluation =
            positiveFunction.evaluate(
                MolecularTestFactory.withIhcTests(
                    listOf(
                        ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS.first()),
                        ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first())
                    )
                )
            )
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if HER2 IHC test results indicate positive HER2 status")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 positive"))
    }

    @Test
    fun `Should evaluate HER2 positive to warn if no certain IHC result available`() {
        val evaluation = positiveFunction.evaluate(
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
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if HER2 IHC test results indicate positive HER2 status")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 positive"))
    }

    @Test
    fun `Should evaluate HER2 positive to warn if ERBB2 is amplified and no certain IHC result available`() {
        val evaluation = positiveFunction.evaluate(
            MolecularTestFactory.withCopyNumberAndIhcTests(
                ERBB2_AMP,
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

    @Test
    fun `Should fail HER2 positive if negative IHC HER2 data but HER2 amp`() {
        val evaluation =
            positiveFunction.evaluate(MolecularTestFactory.withCopyNumberAndIhcTests(ERBB2_AMP, listOf(ihcTest(scoreValue = 0.0))))
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should pass HER2 negative if only negative IHC HER2 data`() {
        val evaluation = negativeFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 0.0),
                    ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should fail HER2 negative if only low or positive IHC HER2 data`() {
        val evaluation = negativeFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 1.0, scoreValueUnit = "+"),
                    ihcTest(scoreValue = 2.0, scoreValueUnit = "+"),
                    ihcTest(scoreValue = 3.0, scoreValueUnit = "+"),
                    ihcTest(scoreText = IhcTestEvaluationConstants.POSITIVE_TERMS.first()),
                    ihcTest(scoreText = IhcTestEvaluationConstants.LOW_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should evaluate HER2 negative to warn if negative IHC HER2 data but HER2 amp`() {
        val evaluation =
            negativeFunction.evaluate(MolecularTestFactory.withCopyNumberAndIhcTests(ERBB2_AMP, listOf(ihcTest(scoreValue = 0.0))))
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if HER2 IHC test results indicate negative HER2 status (but ERBB2 amplification detected)")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 negative"))
    }

    @Test
    fun `Should pass HER2 low if only low IHC HER2 data`() {
        val evaluation = lowFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 1.0, scoreValueUnit = "+"),
                    ihcTest(scoreText = IhcTestEvaluationConstants.LOW_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should fail HER2 low if only negative or positive IHC HER2 data`() {
        val evaluation = lowFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                listOf(
                    ihcTest(scoreValue = 0.0),
                    ihcTest(scoreValue = 3.0, scoreValueUnit = "+"),
                    ihcTest(scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first()),
                    ihcTest(scoreText = IhcTestEvaluationConstants.POSITIVE_TERMS.first())
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should evaluate HER2 low to warn if low IHC HER2 data but HER2 amp`() {
        val evaluation = lowFunction.evaluate(MolecularTestFactory.withCopyNumberAndIhcTests(ERBB2_AMP, listOf(ihcTest(scoreValue = 1.0))))
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Undetermined if HER2 IHC test results indicate low HER2 status (but ERBB2 amplification detected)")
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("Potential IHC HER2 low"))
    }

    @Test
    fun `Should evaluate HER2 low to undetermined for borderline IHC HER2 data`() {
        val evaluation = lowFunction.evaluate(MolecularTestFactory.withIhcTests(listOf(ihcTest(scoreValue = 2.0, scoreValueUnit = "+"))))
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.isMissingMolecularResultForEvaluation).isTrue

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