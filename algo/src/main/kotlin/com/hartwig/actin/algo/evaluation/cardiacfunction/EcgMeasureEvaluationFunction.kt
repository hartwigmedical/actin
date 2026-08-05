package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure

class EcgMeasureEvaluationFunction internal constructor(
    private val measureName: EcgMeasureName,
    private val threshold: Double,
    private val expectedUnit: EcgUnit,
    private val extractingEcgMeasure: (Ecg) -> EcgMeasure?,
    private val thresholdCriteria: ThresholdCriteria,
    private val labels: EvaluationLabels.CardiacFunction
) : EvaluationFunction {

    internal enum class ThresholdCriteria(val comparator: Comparator<Number>) {
        MAXIMUM(Comparator.comparingDouble { obj: Number -> obj.toDouble() }.reversed()),
        MINIMUM(Comparator.comparingDouble { obj: Number -> obj.toDouble() })
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val ecgMeasures = record.ecgs.mapNotNull { ecg -> extractingEcgMeasure(ecg)?.let { ecg to it } }
        val filtered = ecgMeasures.filter { it.second.unit == expectedUnit.symbol() }

        return when {
            ecgMeasures.isEmpty() -> EvaluationFactory.recoverableUndetermined(labels.ecgMeasureEvaluationFunctionNoIntervalKnown(measureName))
            filtered.isEmpty() -> {
                val units = Format.concat(ecgMeasures.map { it.second.unit })
                EvaluationFactory.recoverableUndetermined(
                    measureName.name + labels.ecgMeasureEvaluationFunctionWrongUnit(units, expectedUnit.symbol())
                )
            }
            filtered.size == 1 || filtered.all { with(it.first) { year != null && month != null } } -> {
                evaluate(filtered.maxBy { with(it.first) { "$year-$month" } }.second)
            }
            else -> {
                val evaluations = filtered.map { evaluate(it.second) }
                if (evaluations.map(Evaluation::result).toSet().size == 1) {
                    evaluations.first()
                } else {
                    EvaluationFactory.undetermined(labels.ecgMeasureEvaluationFunctionConflicting(measureName.name))
                }
            }
        }
    }

    private fun evaluate(measure: EcgMeasure): Evaluation {
        return if (thresholdCriteria.comparator.compare(measure.value, threshold) >= 0) {
            val message = when (thresholdCriteria) {
                ThresholdCriteria.MAXIMUM -> labels.ecgMeasureEvaluationFunctionMaximumPass(measureName, measure.value, measure.unit, threshold)
                ThresholdCriteria.MINIMUM -> labels.ecgMeasureEvaluationFunctionMinimumPass(measureName, measure.value, measure.unit, threshold)
            }
            EvaluationFactory.recoverablePass(message)
        } else {
            val message = when (thresholdCriteria) {
                ThresholdCriteria.MAXIMUM -> labels.ecgMeasureEvaluationFunctionMaximumFail(measureName, measure.value, measure.unit, threshold)
                ThresholdCriteria.MINIMUM -> labels.ecgMeasureEvaluationFunctionMinimumFail(measureName, measure.value, measure.unit, threshold)
            }
            EvaluationFactory.recoverableFail(message)
        }
    }
}