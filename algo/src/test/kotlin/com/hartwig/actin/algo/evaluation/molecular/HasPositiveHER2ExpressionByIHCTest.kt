package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.*
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MEASURE = "measure"

class HasPositiveHER2ExpressionByIHCTest {
    val function = HasPositiveHER2ExpressionByIHC()
    private val her2Test = MolecularTestFactory.priorMolecularTest(test = "IHC", item = "HER2", measure = MEASURE)
    val erbb2Amp = TestCopyNumberFactory.createMinimal().copy(
            isReportable = true,
            gene = "ERBB2",
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            type = CopyNumberType.FULL_GAIN,
            minCopies = 20,
            maxCopies = 20
    )

    @Test
    fun `Should evaluate to undetermined when no prior molecular tests available`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList()))
        )
    }

    @Test
    fun `Should evaluate to undetermined when HER2 data is not explicitly positive or negative`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withMolecularTests(
                listOf(
                    IHCMolecularTest(her2Test.copy(scoreText = "nonsense")),
                    IHCMolecularTest(her2Test.copy(scoreText = "more nonsense"))
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to undetermined when HER2 data is conflicting`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withMolecularTests(
                listOf(
                    IHCMolecularTest(
                        her2Test.copy(
                            scoreText = "positive"
                        )
                    ), IHCMolecularTest(
                        her2Test.copy(
                            scoreText = "negative"
                        )
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(
            evaluation.undeterminedGeneralMessages
        ).containsExactly(
            "Conflicting HER2 expression tests by IHC"
        )
    }

    @Test
    fun `Should evaluate to undetermined if positive HER2 data is not reliable`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withMolecularTests(
                listOf(
                    IHCMolecularTest(
                        her2Test.copy(
                            scoreValue = 3.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true
                        )
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to pass if positive HER2 data`() {
        val evaluationValue = function.evaluate(
            MolecularTestFactory.withMolecularTests(
                listOf(
                    IHCMolecularTest(her2Test.copy(scoreValue = 3.0, scoreValueUnit = "+")), IHCMolecularTest(
                        her2Test.copy(
                            scoreValue = 2.0, scoreValueUnit = "+", impliesPotentialIndeterminateStatus = true
                        )
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, evaluationValue)

        val evaluationText = function.evaluate(
            MolecularTestFactory.withMolecularTests(
                listOf(
                    IHCMolecularTest(her2Test.copy(scoreText = "pos", impliesPotentialIndeterminateStatus = true)),
                    IHCMolecularTest(her2Test.copy(scoreText = "positive"))
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, evaluationText)
    }

    @Test
    fun `Should evaluate to warn if ERBB2 is amp and no valid IHC result available`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withCopyNumberAndPriorMolecularTests(
                erbb2Amp,
                listOf(
                    IHCMolecularTest(
                        her2Test.copy(
                            scoreValue = 2.0, scoreValueUnit = "+"
                        )
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(
            evaluation.warnGeneralMessages
        ).containsExactly(
            "Non-positive HER2 IHC results inconsistent with ERBB2 amp"
        )
    }

    @Test
    fun `Should resolve to undetermined if no positive but borderline result`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withMolecularTests(
                listOf(
                    IHCMolecularTest(
                        her2Test.copy(
                            scoreValue = 2.0, scoreValueUnit = "+"
                        )
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(
            evaluation.undeterminedGeneralMessages
        ).containsExactly(
            "HER2 expression by IHC was borderline, additional tests should be considered"
        )
    }

    @Test
    fun `Should fail if no positive result`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withMolecularTests(
                listOf(
                    IHCMolecularTest(
                        her2Test.copy(
                            scoreValue = 1.0, scoreValueUnit = "+"
                        )
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    //        val evaluation = function.evaluate(MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(her2Test.copy(scoreValue = 1.0, scoreText = "positive", scoreValueUnit = "+")))))
}