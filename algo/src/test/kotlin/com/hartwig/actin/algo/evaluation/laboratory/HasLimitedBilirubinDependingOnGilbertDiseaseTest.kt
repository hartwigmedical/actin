package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HasLimitedBilirubinDependingOnGilbertDiseaseTest {

    private val ulnLimitWithoutGilbertDisease = 3.0
    private val ulnLimitWithGilbertDisease = 5.0
    private val refDate = LocalDate.of(2024, 7, 4)
    private val minValidDate = refDate.minusDays(90)
    private val minPassDate = refDate.minusDays(30)
    private val function = HasLimitedBilirubinDependingOnGilbertDisease(
        ulnLimitWithoutGilbertDisease,
        ulnLimitWithGilbertDisease,
        minValidDate,
        minPassDate,
        TestIcdFactory.createTestModel()
    )
    private val TBIL = LabMeasurement.TOTAL_BILIRUBIN
    private val TBIL_1_ULN = LabTestFactory.create(TBIL, value = 100.0, refDate, refLimitUp = 100.0)
    private val TBIL_4_ULN = LabTestFactory.create(TBIL, value = 400.0, refDate, refLimitUp = 100.0)
    private val TBIL_6_ULN = LabTestFactory.create(TBIL, value = 600.0, refDate, refLimitUp = 100.0)
    private val recordWithGilbertDisease = OtherConditionTestFactory.withOtherCondition(
        OtherConditionTestFactory.otherCondition(
            name = "Gilbert",
            icdMainCode = IcdConstants.GILBERT_SYNDROME_CODE
        )
    )
    private val recordWithOtherDisease = OtherConditionTestFactory.withOtherCondition(
        OtherConditionTestFactory.otherCondition(
            name = "Ulcer",
            icdMainCode = IcdConstants.ISCHEMIC_SKIN_ULCER_CODE
        )
    )

    @Test
    fun `Should pass when evaluating 1 times ULN with or without Gilbert disease`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(recordWithOtherDisease.copy(labValues = listOf(TBIL_1_ULN))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(TBIL_1_ULN))))
    }

    @Test
    fun `Should fail when evaluating 4 times ULN and no Gilbert disease`() {
        val evaluation = function.evaluate(recordWithOtherDisease.copy(labValues = listOf(TBIL_4_ULN)))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("Total bilirubin 400.0 umol/L exceeds max of 3.0*ULN (300.0)")
    }

    @Test
    fun `Should pass when evaluating 4 times ULN and Gilbert disease`() {
        val evaluation = function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(TBIL_4_ULN)))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages).containsExactly("Total bilirubin 400.0 umol/L below max of 5.0*ULN (500.0)")
    }

    @Test
    fun `Should fail when evaluating 6 times ULN with or without Gilbert disease`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(recordWithOtherDisease.copy(labValues = listOf(TBIL_6_ULN))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(TBIL_6_ULN))))
    }
}