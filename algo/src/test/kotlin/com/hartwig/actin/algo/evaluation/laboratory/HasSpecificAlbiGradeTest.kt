package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.trial.input.datamodel.AlbiGrade
import org.junit.Test
import java.time.LocalDate

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
    }

    @Test
    fun `Should evaluate to undetermined when no bilirubin result present`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValue(albumin))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to undetermined when lab value date is before min valid date`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(bilirubin, albumin.copy(date = LocalDate.of(2024, 12, 31)))))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to undetermined when albumin unit not gram per liter`() {
        val evaluation =
            function.evaluate(LabTestFactory.withLabValues(listOf(albumin.copy(unit = LabUnit.GRAMS_PER_DECILITER), bilirubin)))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate to undetermined when bilirubin unit not micromoles per liter`() {
        val evaluation =
            function.evaluate(LabTestFactory.withLabValues(listOf(albumin, bilirubin.copy(unit = LabUnit.MILLIMOLES_PER_LITER))))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should pass when ALBI grade matches`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(albumin, bilirubin)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should fail when ALBI grade does not match`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(albumin.copy(value = 20.0), bilirubin)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
    }
}