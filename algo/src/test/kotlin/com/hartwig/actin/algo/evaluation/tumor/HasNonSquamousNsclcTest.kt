package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class HasNonSquamousNsclcTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
    private val function = HasNonSquamousNsclc(doidModel)

    @Test
    fun `Should return undetermined when no tumor doids configured`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should return fail when tumor is not lung`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("wrong")))
    }

    @Test
    fun `Should return fail when squamous NSCLC type`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID))
        )
    }

    @Test
    fun `Should return fail when adenosquamous NSCLC type`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID))
        )
    }

    @Test
    fun `Should return warn when known non-squamous NSCLC type but possible SCC transition`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(IhcTest(item = "SCC transformation", scoreText = "Possible")),
                setOf(DoidConstants.LUNG_ADENOCARCINOMA_DOID)
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Has non-squamous NSCLC but also possibly positive SCC transformation results")
    }

    @Test
    fun `Should return warn when known non-squamous NSCLC type but certain SCC transition`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(IhcTest(item = "SCC transformation", scoreText = "Positive")),
                setOf(DoidConstants.LUNG_ADENOCARCINOMA_DOID)
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings()).containsExactly("Has non-squamous NSCLC but also positive SCC transformation results")
    }

    @Test
    fun `Should return pass when known non-squamous NSCLC type`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOCARCINOMA_DOID)))
    }

    @Test
    fun `Should return pass when known non-squamous NSCLC type with other random DOID`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOCARCINOMA_DOID, "random DOID"))
        )
    }

    @Test
    fun `Should return undetermined when lung cancer that is potentially non-squamous NSCLC`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_CANCER_DOID)))
    }

    @Test
    fun `Should return fail when lung cancer that is not potentially non-squamous NSCLC`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_SARCOMA_DOID)))
    }
}