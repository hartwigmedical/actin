package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.HeartMeasurement
import com.hartwig.actin.datamodel.clinical.HeartMeasurementType

class HeartMeasurementEvaluationFunction internal constructor(
    private val measureType: HeartMeasurementType,
    private val threshold: Double,
    private val expectedUnit: EcgUnit,
    private val thresholdCriteria: ThresholdCriteria
) : EvaluationFunction {

    internal enum class ThresholdCriteria(
        val comparator: Comparator<Number>, val failMessageTemplate: String, val passMessageTemplate: String
    ) {
        MAXIMUM(
            Comparator.comparingDouble { obj: Number -> obj.toDouble() }.reversed(),
            "%s of %s %s is above or equal to max threshold of %s",
            "%s of %s %s does not exceed max threshold of %s",
        ),
        MINIMUM(
            Comparator.comparingDouble { obj: Number -> obj.toDouble() },
            "%s of %s %s is below or equal to min threshold of %s",
            "%s of %s %s exceeds min threshold of %s",
        )
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val ecgMeasures = record.heartMeasurements.filter { it.measurementType == measureType }
        val filtered = ecgMeasures.filter { it.unit == expectedUnit.symbol() }

        return when {
            ecgMeasures.isEmpty() -> EvaluationFactory.recoverableUndetermined(String.format("No %s interval known", measureType))
            filtered.isEmpty() -> {
                val units = Format.concat(ecgMeasures.map { it.unit ?: "" })
                EvaluationFactory.recoverableUndetermined(
                    "${measureType.name} measure in $units instead of required ${expectedUnit.symbol()}"
                )
            }

            filtered.size == 1 || filtered.all { it.year != null && it.month != null } -> {
                evaluate(filtered.maxBy { "${it.year}-${it.month}" })
            }
            else -> {
                val evaluations = filtered.map { evaluate(it) }
                if (evaluations.map(Evaluation::result).toSet().size == 1) {
                    evaluations.first()
                } else {
                    EvaluationFactory.undetermined("Conflicting evaluations for ${measureType.name} with unknown dates")
                }
            }
        }
    }

    private fun evaluate(measure: HeartMeasurement): Evaluation {
        return if (thresholdCriteria.comparator.compare(measure.value, threshold) >= 0) {
            EvaluationFactory.recoverablePass(
                String.format(thresholdCriteria.passMessageTemplate, measureType, measure.value, measure.unit, threshold)
            )
        } else {
            EvaluationFactory.recoverableFail(
                String.format(thresholdCriteria.failMessageTemplate, measureType, measure.value, measure.unit, threshold)
            )
        }
    }
}