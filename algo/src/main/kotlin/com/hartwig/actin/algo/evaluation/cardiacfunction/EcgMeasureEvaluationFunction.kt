package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
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
        val ecgMeasures = record.ecgs.mapNotNull { ecg -> extractingEcgMeasure(ecg)?.let { ecg to it } }
        val filtered = ecgMeasures.filter { it.second.unit == expectedUnit.symbol() }

        return when {
            ecgMeasures.isEmpty() -> EvaluationFactory.recoverableUndetermined(String.format("No %s interval known", measureName))
            filtered.isEmpty() -> {
                val units = Format.concat(ecgMeasures.map { it.second.unit })
                EvaluationFactory.recoverableUndetermined(
                    "${measureName.name} measure in $units instead of required ${expectedUnit.symbol()}"
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
                    EvaluationFactory.undetermined("Conflicting evaluations for ${measureName.name} with unknown dates")
                }
            }
        }
    }

    private fun evaluate(measure: EcgMeasure): Evaluation {
        return if (thresholdCriteria.comparator.compare(measure.value, threshold) >= 0) {
            EvaluationFactory.recoverablePass(
                String.format(thresholdCriteria.passMessageTemplate, measureName, measure.value, measure.unit, threshold)
            )
        } else {
            EvaluationFactory.recoverableFail(
                String.format(thresholdCriteria.failMessageTemplate, measureName, measure.value, measure.unit, threshold)
            )
        }
    }
}