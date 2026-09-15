package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestItemConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasNonSquamousNsclcTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
    private val function = HasNonSquamousNsclc(doidModel)

    @Test
    fun `Should return undetermined when no tumor doids configured`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Non-squamous NSCLC tumor type undetermined (tumor type missing)"
        )
    }

    @Test
    fun `Should return fail when tumor is not lung`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids("wrong")),
            "Cancer is not non-squamous NSCLC"
        )
    }

    @Test
    fun `Should return fail when squamous NSCLC type`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID)),
            "Cancer is not non-squamous NSCLC"
        )
    }

    @Test
    fun `Should return fail when adenosquamous NSCLC type`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID)),
            "Cancer is not non-squamous NSCLC"
        )
    }

    @Test
    fun `Should return warn when known non-squamous NSCLC type but possible SCC transformation`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(IhcTest(item = IhcTestItemConstants.SCC_TRANSFORMATION_TERM, scoreText = "Possible")),
                setOf(DoidConstants.LUNG_ADENOCARCINOMA_DOID)
            )
        )
        assertEvaluation(
            EvaluationResult.WARN,
            evaluation,
            "Cancer is non-squamous NSCLC but also possibly positive SCC transformation results"
        )
    }

    @Test
    fun `Should return warn when known non-squamous NSCLC type but certain SCC transformation`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(IhcTest(item = IhcTestItemConstants.SCC_TRANSFORMATION_TERM, scoreText = "Positive")),
                setOf(DoidConstants.LUNG_ADENOCARCINOMA_DOID)
            )
        )
        assertEvaluation(
            EvaluationResult.WARN,
            evaluation,
            "Cancer is non-squamous NSCLC but also positive SCC transformation results"
        )
    }

    @Test
    fun `Should return pass when known non-squamous NSCLC type`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOCARCINOMA_DOID)),
            "Cancer is non-squamous NSCLC"
        )
    }

    @Test
    fun `Should return pass when known non-squamous NSCLC type with other random DOID`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_ADENOCARCINOMA_DOID, "random DOID")),
            "Cancer is non-squamous NSCLC"
        )
    }

    @Test
    fun `Should return undetermined when lung cancer that is potentially non-squamous NSCLC`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_CANCER_DOID)),
            "Undetermined if non-squamous NSCLC"
        )
    }

    @Test
    fun `Should return fail when lung cancer that is not potentially non-squamous NSCLC`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_SARCOMA_DOID)),
            "Cancer is not non-squamous NSCLC"
        )
    }
}