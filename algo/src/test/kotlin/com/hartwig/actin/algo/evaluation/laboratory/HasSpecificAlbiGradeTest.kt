package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AlbiGrade
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasSpecificAlbiGradeTest {

    private val targetGrade = AlbiGrade.GRADE_2
    private val minValidDate = LocalDate.of(2025, 1, 1)
    private val minPassDate = LocalDate.of(2025, 2, 1)
    private val function = HasSpecificAlbiGrade(targetGrade, minValidDate, minPassDate)
    private val albumin = LabTestFactory.create(LabMeasurement.ALBUMIN, 35.0, minPassDate)
    private val bilirubin = LabTestFactory.create(LabMeasurement.TOTAL_BILIRUBIN, 10.0, minPassDate)

    @Test
    fun `Should evaluate to undetermined when no albumin result present`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValue(bilirubin))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should evaluate to undetermined when no bilirubin result present`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValue(albumin))
        assertThat(evaluation.recoverable).isTrue
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to undetermined when lab value date is before min valid date`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(bilirubin, albumin.copy(date = LocalDate.of(2024, 12, 31)))))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should pass when ALBI grade matches`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(albumin, bilirubin)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.recoverable).isTrue
        assertThat(evaluation.passMessagesStrings()).containsExactly("ALBI grade sufficient")
    }

    @Test
    fun `Should fail when ALBI grade does not match`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(albumin.copy(value = 20.0), bilirubin)))
        assertThat(evaluation.recoverable).isTrue
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "ALBI grade (grade 3) insufficient - should be ${targetGrade.display()}"
        )
    }
}