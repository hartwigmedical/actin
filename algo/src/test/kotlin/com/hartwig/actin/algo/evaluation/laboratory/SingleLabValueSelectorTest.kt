package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
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

    @Test
    fun `Should return NotFound with undetermined when most recent value has non-convertible unit`() {
        val record = LabTestFactory.withLabValue(LabTestFactory.create(measurement, date = today).copy(unit = LabUnit.SECONDS))
        val interpretation = LabInterpretation.interpret(record.labValues)
        val result = SingleLabValueSelector(measurement).select(interpretation, minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should return Found with converted unit when unit is convertible to default`() {
        val record = LabTestFactory.withLabValue(
            LabTestFactory.create(measurement, date = today).copy(unit = LabUnit.GRAMS_PER_DECILITER, value = 4.0)
        )
        val interpretation = LabInterpretation.interpret(record.labValues)
        val result = SingleLabValueSelector(measurement).select(interpretation, minValidDate) as LabValueSelectionResult.Found
        assertThat(result.values[measurement]!!.unit).isEqualTo(measurement.defaultUnit)
    }
}
