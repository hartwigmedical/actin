package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
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
        return record.ecgs.firstOrNull()?.let(extractingEcgMeasure)
            ?.let { measure: EcgMeasure -> this.evaluate(measure) }
            ?: EvaluationFactory.recoverableUndetermined(String.format("No %s known", measureName))
    }

    private fun evaluate(measure: EcgMeasure): Evaluation {
        if (measure.unit != expectedUnit.symbol()) {
            return EvaluationFactory.undetermined("${measureName.name} measure in ${measure.unit} instead of required ${expectedUnit.symbol()}")
        }

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