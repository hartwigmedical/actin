package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TestPriorSecondPrimaryFactory
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test


class HasKnownSCLCTransformationTest {

    private val function = HasKnownSCLCTransformation(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun `Should evaluate to undetermined if no tumor doids configured`(){
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord())
        )
    }

    @Test
    fun `Should pass if current tumor is SCLC and prior NSCLC in history`(){
        val history = TestPatientFactory.createMinimalTestPatientRecord().copy(
            priorSecondPrimaries = listOf(
                TestPriorSecondPrimaryFactory.createMinimal().copy(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID))
            ),
            tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_SMALL_CELL_CARCINOMA_DOID))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(history))
    }

    @Test
    fun `Should evaluate to undetermined if current tumor is SCLC with no known prior primaries`(){
        val history = TestPatientFactory.createMinimalTestPatientRecord().copy(
            priorSecondPrimaries = emptyList(),
            tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_SMALL_CELL_CARCINOMA_DOID))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(history))
    }

    @Test
    fun `Should evaluate to undetermined if current tumor is SCLC with prior primary other than NSCLC type`(){
        val history = TestPatientFactory.createMinimalTestPatientRecord().copy(
            priorSecondPrimaries = listOf(
                TestPriorSecondPrimaryFactory.createMinimal().copy(doids = setOf(DoidConstants.STOMACH_CANCER_DOID))
            ),
            tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_SMALL_CELL_CARCINOMA_DOID))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(history))
    }

    @Test
    fun `Should fail if current tumor is not SCLC`(){
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.STOMACH_CANCER_DOID))
        )
    }
}