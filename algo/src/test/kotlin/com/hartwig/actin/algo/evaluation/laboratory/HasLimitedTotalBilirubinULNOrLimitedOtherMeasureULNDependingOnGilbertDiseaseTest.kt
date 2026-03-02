package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.icd.TestIcdFactory
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
    private val tbil1Uln = LabTestFactory.create(LabMeasurement.TOTAL_BILIRUBIN, value = 100.0, refDate, refLimitUp = 100.0)
    private val tbil4Uln = tbil1Uln.copy(value = 400.0)
    private val dbil1Uln = LabTestFactory.create(LabMeasurement.DIRECT_BILIRUBIN, value = 100.0, refDate, refLimitUp = 100.0)
    private val dbil6Uln = dbil1Uln.copy(value = 600.0)
    private val recordWithGilbertDisease = ComorbidityTestFactory.withOtherCondition(
        ComorbidityTestFactory.otherCondition(
            name = "Gilbert",
            icdMainCode = IcdConstants.GILBERT_SYNDROME_CODE
        )
    )
    private val recordWithoutGilbertDisease = ComorbidityTestFactory.withOtherConditions(emptyList())

    @Test
    fun `Should pass when evaluating requested and allowed measure`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(recordWithoutGilbertDisease.copy(labValues = listOf(tbil1Uln))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(dbil1Uln))))
    }

    @Test
    fun `Should be undetermined evaluating unrequested measure`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(recordWithoutGilbertDisease.copy(labValues = listOf(dbil1Uln))))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(tbil1Uln))))
    }

    @Test
    fun `Should fail when evaluating required measure but exceeding ULN in case of no Gilbert disease`() {
        val evaluation = function.evaluate(recordWithoutGilbertDisease.copy(labValues = listOf(tbil4Uln)))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly("Total bilirubin 400.0 umol/L exceeds max of 3.0*ULN (300.0 umol/L)")
    }

    @Test
    fun `Should fail when evaluating required measure but exceeding ULN in case of Gilbert disease`() {
        val evaluation = function.evaluate(recordWithGilbertDisease.copy(labValues = listOf(dbil6Uln)))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly("Direct bilirubin 600.0 umol/L exceeds max of 5.0*ULN (500.0 umol/L)")
    }
}