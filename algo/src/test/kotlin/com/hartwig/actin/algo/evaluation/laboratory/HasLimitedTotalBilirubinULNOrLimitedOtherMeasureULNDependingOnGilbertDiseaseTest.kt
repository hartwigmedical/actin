package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HasLimitedTotalBilirubinULNOrLimitedOtherMeasureULNDependingOnGilbertDiseaseTest {

    private val ulnLimitWithoutGilbertDisease = 3.0
    private val ulnLimitWithGilbertDisease = 5.0
    private val refDate = LocalDate.of(2024, 7, 4)
    private val minValidDate = refDate.minusDays(90)
    private val minPassDate = refDate.minusDays(30)
    private val function = HasLimitedTotalBilirubinULNOrLimitedOtherMeasureULNDependingOnGilbertDisease(
        ulnLimitWithoutGilbertDisease,
        LabMeasurement.DIRECT_BILIRUBIN,
        ulnLimitWithGilbertDisease,
        minValidDate,
        minPassDate,
        TestIcdFactory.createTestModel()
    )
    private val TBIL_1_ULN = LabTestFactory.create(LabMeasurement.TOTAL_BILIRUBIN, value = 100.0, refDate, refLimitUp = 100.0)
    private val TBIL_4_ULN = TBIL_1_ULN.copy(value = 400.0)
    private val DBIL_1_ULN = LabTestFactory.create(LabMeasurement.DIRECT_BILIRUBIN, value = 100.0, refDate, refLimitUp = 100.0)
    private val DBIL_6_ULN = DBIL_1_ULN.copy(value = 600.0)
    private val recordWithGilbertDisease = ComorbidityTestFactory.withOtherCondition(
        ComorbidityTestFactory.otherCondition(
            name = "Gilbert",
            icdMainCode = IcdConstants.GILBERT_SYNDROME_CODE
        )
    )
    private val recordWithoutGilbertDisease = ComorbidityTestFactory.withOtherConditions(emptyList())

    @Test
    fun `Should pass when evaluating requested and allowed measure`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(recordWithoutGilbertDisease.copy(labValues = listOf(TBIL_1_ULN))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(DBIL_1_ULN))))
    }

    @Test
    fun `Should be undetermined evaluating unrequested measure`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(recordWithoutGilbertDisease.copy(labValues = listOf(DBIL_1_ULN))))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(TBIL_1_ULN))))
    }

    @Test
    fun `Should fail when evaluating required measure but exceeding ULN in case of no Gilbert disease`() {
        val evaluation = function.evaluate(recordWithoutGilbertDisease.copy(labValues = listOf(TBIL_4_ULN)))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly("Total bilirubin 400.0 umol/L exceeds max of 3.0*ULN (300.0 umol/L)")
    }

    @Test
    fun `Should fail when evaluating required measure but exceeding ULN in case of Gilbert disease`() {
        val evaluation = function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(DBIL_6_ULN)))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly("Direct bilirubin 600.0 umol/L exceeds max of 5.0*ULN (500.0 umol/L)")
    }
}