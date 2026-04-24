package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

internal class MultiLabMeasurementEvaluator(
    private val measurements: Set<LabMeasurement>,
    private val function: MultiLabEvaluationFunction,
    private val minValidDate: LocalDate,
    private val minPassDate: LocalDate,
    private val requireSameDate: Boolean = true,
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpretation = LabInterpretation.interpret(record.labValues)
        val selectedValues = if (requireSameDate) {
            findSameDateValues(interpretation)
        } else {
            findIndependentValues(interpretation)
        } ?: return EvaluationFactory.recoverableUndetermined(
            "No date found with all required lab values: ${measurements.joinToString { it.display() }}"
        )

        val evaluation = function.evaluate(record, selectedValues)

        val oldestDate = selectedValues.values.minOf { it.date }
        return if (evaluation.result == EvaluationResult.PASS && !oldestDate.isAfter(minPassDate)) {
            Evaluation(
                result = EvaluationResult.PASS,
                recoverable = true,
                passMessages = evaluation.passMessages
                    .map { StaticMessage("$it but measurement occurred before ${Format.date(minPassDate)}") }
                    .toSet()
            )
        } else evaluation
    }

    private fun findSameDateValues(interpretation: LabInterpretation): Map<LabMeasurement, LabValue>? {
        val sharedDates = measurements
            .map { measurement ->
                interpretation.allValues(measurement)
                    ?.sortedByDescending { it.date }
                    ?.map { it.date }?.toSet() ?: emptySet()
            }
            .reduceOrNull { accumulator, dates -> accumulator intersect dates }
            ?.filter { !it.isBefore(minValidDate) }
            ?.sortedDescending()

        return sharedDates?.firstNotNullOfOrNull { date ->
            measurements.associateWith { measurement ->
                interpretation.valuesOnDate(measurement, date)
                    .firstOrNull { LabEvaluation.isValid(it, measurement, minValidDate) }
            }
                .takeUnless { it.values.any { it == null } }
                ?.mapValues { it.value!! }
        }
    }

    private fun findIndependentValues(interpretation: LabInterpretation): Map<LabMeasurement, LabValue>? {
        return measurements
            .associateWith { measurement ->
                interpretation.allValues(measurement)
                    ?.sortedByDescending { it.date }
                    ?.filter { !it.date.isBefore(minValidDate) }
                    ?.firstOrNull { value -> LabEvaluation.isValid(value, measurement, minValidDate) }
            }
            .takeUnless { it.values.any { it == null } }
            ?.mapValues { it.value!! }
    }
}
