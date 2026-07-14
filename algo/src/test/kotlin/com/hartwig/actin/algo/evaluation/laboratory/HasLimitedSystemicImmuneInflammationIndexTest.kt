package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasLimitedSystemicImmuneInflammationIndexTest {

    private val minValidDate = LocalDate.of(2025, 1, 1)
    private val minPassDate = LocalDate.of(2025, 2, 1)
    private val function = HasLimitedSystemicImmuneInflammationIndex(900.0, minValidDate, minPassDate)
    private val neutrophils = LabTestFactory.create(LabMeasurement.NEUTROPHILS_ABS, 2.5, minPassDate)
    private val thrombocytes = LabTestFactory.create(LabMeasurement.THROMBOCYTES_ABS, 50.0, minPassDate)
    private val lymphocytes = LabTestFactory.create(LabMeasurement.LYMPHOCYTES_ABS, 1.0, minPassDate)

    @Test
    fun `Should evaluate to undetermined when no lymphocytes result present`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(thrombocytes, neutrophils)))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should evaluate to undetermined when lab value date is before min valid date`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(neutrophils, thrombocytes, lymphocytes.copy(date = LocalDate.of(2024, 12, 31)))))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should pass when systemic immune inflammation index sufficient`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(neutrophils, thrombocytes, lymphocytes)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.recoverable).isTrue
        assertThat(evaluation.passMessagesStrings()).containsExactly("Systemic immune-inflammation index at most 900.0")
    }

    @Test
    fun `Should fail when systemic immune inflammation index insufficient`() {
        val evaluation = function.evaluate(LabTestFactory.withLabValues(listOf(neutrophils.copy(value = 10.0), thrombocytes.copy(value = 150.0), lymphocytes)))
        assertThat(evaluation.recoverable).isTrue
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly("Systemic immune-inflammation index above 900.0")
    }
}