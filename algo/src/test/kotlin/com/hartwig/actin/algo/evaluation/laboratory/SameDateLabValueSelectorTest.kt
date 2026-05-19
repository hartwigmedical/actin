package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SameDateLabValueSelectorTest {

    private val measurement1 = LabMeasurement.entries[0]
    private val measurement2 = LabMeasurement.entries[1]
    private val today = LocalDate.now()
    private val minValidDate = today.minusDays(90)

    private fun selector() = SameDateLabValueSelector(setOf(measurement1, measurement2))

    @Test
    fun `Should return Found when both measurements share a valid date`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = today),
            LabTestFactory.create(measurement2, date = today)
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.Found::class.java)
    }

    @Test
    fun `Should return NotFound when measurements have no shared date`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = today),
            LabTestFactory.create(measurement2, date = today.minusDays(1))
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should return NotFound when shared date is before minValidDate`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = minValidDate.minusDays(1)),
            LabTestFactory.create(measurement2, date = minValidDate.minusDays(1))
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should return NotFound when one value has wrong unit on shared date`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = today).copy(unit = LabUnit.SECONDS),
            LabTestFactory.create(measurement2, date = today)
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should return NotFound when all values have wrong unit on shared date`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = today).copy(unit = LabUnit.SECONDS),
            LabTestFactory.create(measurement2, date = today).copy(unit = LabUnit.SECONDS)
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should use most recent shared date when older shared dates also exist`() {
        val olderDate = today.minusDays(5)
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = today),
            LabTestFactory.create(measurement2, date = today),
            LabTestFactory.create(measurement1, date = olderDate),
            LabTestFactory.create(measurement2, date = olderDate)
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.Found::class.java)
        assertThat((result as LabValueSelectionResult.Found).values.values).allMatch { it.date == today }
    }

    @Test
    fun `Should return NotFound and not fall back to older date when most recent shared date is invalid`() {
        val olderDate = today.minusDays(5)
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = today).copy(unit = LabUnit.SECONDS),
            LabTestFactory.create(measurement2, date = today),
            LabTestFactory.create(measurement1, date = olderDate),
            LabTestFactory.create(measurement2, date = olderDate)
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should return Found with converted units and notes when all units are convertible to default`() {
        val albumin = LabMeasurement.ALBUMIN
        val creatinine = LabMeasurement.CREATININE
        val record = LabTestFactory.withLabValues(
            listOf(
                LabTestFactory.create(albumin, date = today).copy(unit = LabUnit.GRAMS_PER_DECILITER, value = 4.0),
                LabTestFactory.create(creatinine, date = today).copy(unit = LabUnit.MILLIGRAMS_PER_DECILITER, value = 1.0)
            )
        )
        val result = SameDateLabValueSelector(setOf(albumin, creatinine)).select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.Found::class.java)
        result as LabValueSelectionResult.Found
        assertThat(result.values[albumin]!!.unit).isEqualTo(albumin.defaultUnit)
        assertThat(result.values[creatinine]!!.unit).isEqualTo(creatinine.defaultUnit)
        assertThat(result.conversionNotes).hasSize(2)
    }

    @Test
    fun `Should return NotFound when one value has non-convertible unit on shared date`() {
        val albumin = LabMeasurement.ALBUMIN
        val creatinine = LabMeasurement.CREATININE
        val record = LabTestFactory.withLabValues(
            listOf(
                LabTestFactory.create(albumin, date = today).copy(unit = LabUnit.SECONDS),
                LabTestFactory.create(creatinine, date = today).copy(unit = LabUnit.MILLIGRAMS_PER_DECILITER, value = 1.0)
            )
        )
        val result = SameDateLabValueSelector(setOf(albumin, creatinine)).select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.NotFound::class.java)
        assertEvaluation(EvaluationResult.UNDETERMINED, (result as LabValueSelectionResult.NotFound).evaluation)
    }

    @Test
    fun `Should return Found with no conversion notes when units already match default`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(measurement1, date = today),
            LabTestFactory.create(measurement2, date = today)
        ))
        val result = selector().select(LabInterpretation.interpret(record.labValues), minValidDate)
        assertThat(result).isInstanceOf(LabValueSelectionResult.Found::class.java)
        assertThat((result as LabValueSelectionResult.Found).conversionNotes).isEmpty()
    }
}
