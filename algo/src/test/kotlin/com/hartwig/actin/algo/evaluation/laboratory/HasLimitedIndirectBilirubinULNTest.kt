package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HasLimitedIndirectBilirubinULNTest {

    private val refDate = LocalDate.of(2024, 7, 4)
    private val minValidDate = refDate.minusDays(90)
    private val directBilirubin = LabTestFactory.create(LabMeasurement.DIRECT_BILIRUBIN, 4.5, refDate, refLimitUp = 5.0, refLimitLow = 1.7)
    private val totalBilirubin = LabTestFactory.create(LabMeasurement.TOTAL_BILIRUBIN, value = 16.0, refDate, refLimitUp = 17.0, refLimitLow = 5.0)
    val function = HasLimitedIndirectBilirubinULN(2.0, minValidDate)

    @Test
    fun `Should pass when indirect bilirubin below requested fold of ULN`() {
        val record = LabTestFactory.withLabValue(totalBilirubin)
        val actual = function.evaluate(record, LabMeasurement.DIRECT_BILIRUBIN, directBilirubin)
        assertEvaluation(EvaluationResult.PASS, actual)
    }

    @Test
    fun `Should fail if indirect bilirubin is above requested fold of ULN and outside of margin of error`() {
        val record = LabTestFactory.withLabValue(totalBilirubin.copy(value = 30.0))
        val actual = function.evaluate(record, LabMeasurement.DIRECT_BILIRUBIN, directBilirubin)
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertThat(actual.recoverable).isTrue
    }

    @Test
    fun `Should evaluate to recoverable undetermined if comparison to ULN cannot be made due to missing reference limit`() {
        val record = LabTestFactory.withLabValue(totalBilirubin.copy(refLimitUp = null))
        val actual = function.evaluate(record, LabMeasurement.DIRECT_BILIRUBIN, directBilirubin)
        assertEvaluation(EvaluationResult.UNDETERMINED, actual)
        assertThat(actual.recoverable).isTrue
    }

    @Test
    fun `Should evaluate to undetermined in case of old bilirubin value`() {
        val oldDate = LocalDate.of(2019, 4, 4)
        val record = LabTestFactory.withLabValue(totalBilirubin.copy(date = oldDate))
        val actual = function.evaluate(record, LabMeasurement.DIRECT_BILIRUBIN, directBilirubin)
        assertEvaluation(EvaluationResult.UNDETERMINED, actual)
    }

    @Test
    fun `Should evaluate to recoverable undetermined if indirect bilirubin is above requested fold of ULN but within margin of error`() {
        val record = LabTestFactory.withLabValue(totalBilirubin.copy(value = 29.5))
        val actual = function.evaluate(record, LabMeasurement.DIRECT_BILIRUBIN, directBilirubin)
        assertEvaluation(EvaluationResult.UNDETERMINED, actual)
        assertThat(actual.recoverable).isTrue
    }

    @Test
    fun `Should evaluate to recoverable undetermined if comparison to ULN cannot be made due to missing value`() {
        val record = LabTestFactory.withLabValues(emptyList())
        val actual = function.evaluate(record, LabMeasurement.DIRECT_BILIRUBIN, directBilirubin)
        assertEvaluation(EvaluationResult.UNDETERMINED, actual)
        assertThat(actual.recoverable).isTrue
    }
}