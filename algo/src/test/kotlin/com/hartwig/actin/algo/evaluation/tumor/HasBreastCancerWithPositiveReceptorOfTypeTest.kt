package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.datamodel.clinical.ReceptorType.ER
import com.hartwig.actin.datamodel.clinical.ReceptorType.HER2
import com.hartwig.actin.datamodel.clinical.ReceptorType.PR
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

const val TARGET_RECEPTOR = "PR"

class HasBreastCancerWithPositiveReceptorOfTypeTest {
    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasBreastCancerWithPositiveReceptorOfType(doidModel, ReceptorType.valueOf(TARGET_RECEPTOR))

    @Test
    fun `Should evaluate to undetermined when no tumor doids configured`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest(TARGET_RECEPTOR, "Positive")), emptySet()
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if PR positive breast cancer (tumor doids missing)")
    }

    @Test
    fun `Should fail if tumor type is not breast cancer`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest(TARGET_RECEPTOR, "Positive")),
                    setOf(DoidConstants.COLORECTAL_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if no data is present for target receptor in doids or prior molecular tests`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest("some test", "Positive"), createIhcTest("other test", "Positive")),
                setOf(DoidConstants.BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("$TARGET_RECEPTOR-status unknown (data missing)")
    }

    @Test
    fun `Should evaluate to undetermined if no data is present for target receptor HER2 but ERBB2 amplification found`() {
        val evaluation = HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
            TumorTestFactory.withDoidsAndAmplificationAndMolecularTest(
                setOf(DoidConstants.BREAST_CANCER_DOID), "ERBB2", listOf(createIhcTest("wrong test", "positive"))
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(
            evaluation.undeterminedMessagesStrings()
        ).containsExactly(
            "HER2-status undetermined (IHC data missing) but probably positive since ERBB2 amp present"
        )
    }

    @Test
    fun `Should evaluate to undetermined with specific message if prior molecular test data inconsistent`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest(TARGET_RECEPTOR, "Negative"), createIhcTest(TARGET_RECEPTOR, "Positive")),
                setOf(DoidConstants.BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(
            evaluation.undeterminedMessagesStrings()
        ).containsExactly(
            "$TARGET_RECEPTOR-status undetermined (DOID and/or IHC data inconsistent)"
        )
    }

    @Test
    fun `Should evaluate to undetermined if doids inconsistent`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    emptyList(),
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
                        DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if prior molecular test data inconsistent with doids`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest(TARGET_RECEPTOR, "Negative")),
                    setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should pass if target receptor type is positive with data source doids`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest("HER2", "Negative")),
                    setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should pass if target receptor type is positive with data source prior molecular tests`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest(TARGET_RECEPTOR, "Positive")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should pass if target receptor type is positive with data source scoreValue from prior molecular tests`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest(item = TARGET_RECEPTOR, scoreValue = 75.0, scoreValueUnit = "%")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest(item = "HER2", scoreValue = 3.0, scoreValueUnit = "+")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should warn if HER2 negative based on doids but ERBB2 amp present`() {
        assertEvaluation(
            EvaluationResult.WARN, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withDoidsAndAmplification(
                    setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID), "ERBB2"
                )
            )
        )
    }

    @Test
    fun `Should warn if HER2 negative based on IHC but ERBB2 amp present`() {
        assertEvaluation(
            EvaluationResult.WARN, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withDoidsAndAmplificationAndMolecularTest(
                    setOf(DoidConstants.BREAST_CANCER_DOID), "ERBB2", listOf(
                        createIhcTest("HER2", "Negative")
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn if target receptor is HER2 and unclear (not positive or negative) based on IHC and doids but ERBB2 amp present`() {
        assertEvaluation(
            EvaluationResult.WARN, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withDoidsAndAmplificationAndMolecularTest(
                    setOf(DoidConstants.BREAST_CANCER_DOID),
                    "ERBB2",
                    listOf(createIhcTest("HER2", scoreValue = 1.0, scoreValueUnit = "+"))
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined with specific message if target receptor is HER2 and IHC-score is 2+`() {
        val evaluation = HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest("HER2", scoreValue = 2.0, scoreValueUnit = "+")),
                setOf(DoidConstants.BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(
            evaluation.undeterminedMessagesStrings()
        ).containsExactly(
            "No HER2-positive breast cancer but HER2-score is2+ hence FISH may be useful"
        )
    }

    @Test
    fun `Should warn with specific message if target receptor is ER or PR and IHC-score is between 1 and 10 percent`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest(TARGET_RECEPTOR, scoreValue = 5.0, scoreValueUnit = "%")),
                setOf(DoidConstants.BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(
            evaluation.warnMessagesStrings()
        ).containsExactly(
            "Has $TARGET_RECEPTOR-positive breast cancer but clinical relevance unknown ($TARGET_RECEPTOR-score under 10%)"
        )
    }

    @Test
    fun `Should fail if target molecular test present but no clear determination possible on present data`() {
        assertEvaluation(
            EvaluationResult.FAIL, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest("HER2", "Unclear")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should fail if target receptor type is negative with data source doids`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    emptyList(),
                    setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should fail for all receptor types if tumor has doid term triple negative breast cancer configured`() {
        val record = TumorTestFactory.withIhcTestsAndDoids(
            emptyList(),
            setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.TRIPLE_NEGATIVE_BREAST_CANCER_DOID)
        )
        assertEvaluation(EvaluationResult.FAIL, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(record))
        assertEvaluation(EvaluationResult.FAIL, HasBreastCancerWithPositiveReceptorOfType(doidModel, ER).evaluate(record))
        assertEvaluation(EvaluationResult.FAIL, HasBreastCancerWithPositiveReceptorOfType(doidModel, PR).evaluate(record))
    }

    @Test
    fun `Should fail if target receptor type is negative with data source scoreText from prior molecular tests`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest(TARGET_RECEPTOR, "Negative")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should fail if target receptor type is negative with data source scoreValue from prior molecular tests`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(createIhcTest(TARGET_RECEPTOR, scoreValue = 0.0, scoreValueUnit = "%")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should only use scoreValue from target receptor type in evaluation of prior molecular tests`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(
                        createIhcTest("HER2", scoreValue = 50.0, scoreValueUnit = "%"),
                        createIhcTest(TARGET_RECEPTOR, scoreValue = 0.0, scoreValueUnit = "%")
                    ),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withIhcTestsAndDoids(
                    listOf(
                        createIhcTest("HER2", scoreValue = 1.0, scoreValueUnit = "+"),
                        createIhcTest("PR", scoreValue = 3.0, scoreValueUnit = "%")
                    ),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    private fun createIhcTest(
        item: String, scoreText: String = "Score", scoreValue: Double = 50.0, scoreValueUnit: String = "Unit"
    ) = IhcTest(
        item = item, scoreText = scoreText, scoreValue = scoreValue,
        scoreValueUnit = scoreValueUnit, impliesPotentialIndeterminateStatus = false
    )

}