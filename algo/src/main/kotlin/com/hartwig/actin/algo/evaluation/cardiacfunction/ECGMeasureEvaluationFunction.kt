package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ECG
import com.hartwig.actin.datamodel.clinical.ECGMeasure

class ECGMeasureEvaluationFunction internal constructor(
    private val measureName: ECGMeasureName,
    private val threshold: Double,
    private val expectedUnit: ECGUnit,
    private val extractingECGMeasure: (ECG) -> ECGMeasure?,
    private val thresholdCriteria: ThresholdCriteria
) : EvaluationFunction {

    internal enum class ThresholdCriteria(
        val comparator: Comparator<Number>, val failMessageTemplate: String, val passMessageTemplate: String
    ) {
        MAXIMUM(
            Comparator.comparingDouble { obj: Number -> obj.toDouble() }.reversed(),
            "%s of %s %s does not exceed min threshold of %s",
            "%s of %s %s is above or equal to min threshold of %s"
        ),
        MINIMUM(
            Comparator.comparingDouble { obj: Number -> obj.toDouble() },
            "%s of %s %s exceeds max threshold of %s",
            "%s of %s %s is below or equal to max threshold of %s"
        )
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        return record.clinicalStatus.ecg?.let(extractingECGMeasure)
            ?.let { measure: ECGMeasure -> this.evaluate(measure) }
            ?: EvaluationFactory.recoverableUndetermined(String.format("No %s known", measureName))
    }

    private fun evaluate(measure: ECGMeasure): Evaluation {
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