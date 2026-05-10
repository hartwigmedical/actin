package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SingleLabValueSelectorTest {

    private val measurement = LabMeasurement.ALBUMIN
    private val today = LocalDate.now()
    private val minValidDate = today.minusDays(90)

    @Test
    fun `Should return Found when most recent value is valid`() {
        val record = LabTestFactory.withLabValue(LabTestFactory.create(measurement, date = today))
        val interpretation = LabInterpretation.interpret(record.labValues)
        val result = SingleLabValueSelector(measurement).select(interpretation, minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.Found::class.java)
    }

    @Test
    fun `Should return NotFound with undetermined when no value present`() {
        val record = LabTestFactory.withLabValues(emptyList())
        val interpretation = LabInterpretation.interpret(record.labValues)
        val result = SingleLabValueSelector(measurement).select(interpretation, minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should return NotFound with undetermined when most recent value is too old`() {
        val record = LabTestFactory.withLabValue(LabTestFactory.create(measurement, date = minValidDate.minusDays(1)))
        val interpretation = LabInterpretation.interpret(record.labValues)
        val result = SingleLabValueSelector(measurement).select(interpretation, minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }
}
