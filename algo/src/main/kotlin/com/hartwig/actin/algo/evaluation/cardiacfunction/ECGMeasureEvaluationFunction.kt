package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ECGMeasure

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
            "%s of %s %s does not exceed minimum threshold of %s",
            "%s of %s %s is above or equal to minimum threshold of %s"
        ),
        MINIMUM(
            Comparator.comparingDouble { obj: Number -> obj.toDouble() },
            "%s of %s %s exceeds maximum threshold of %s",
            "%s of %s %s is below or equal to maximum threshold of %s"
        )
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        return record.clinical().clinicalStatus().ecg()?.let(extractingECGMeasure)?.let { measure: ECGMeasure -> this.evaluate(measure) }
            ?: EvaluationFactory.undetermined(
                String.format("No %s known", measureName), String.format("Undetermined %s", measureName)
            )
    }

    private fun evaluate(measure: ECGMeasure): Evaluation {
        if (measure.unit() != expectedUnit.symbol()) {
            return unrecoverable().result(EvaluationResult.UNDETERMINED).addUndeterminedSpecificMessages(
                "%s measure not in '%s': %s", measureName.name, expectedUnit.symbol(), measure.unit()
            ).addUndeterminedGeneralMessages(String.format("Unrecognized unit of %s evaluation", measureName)).build()
        }
        val result =
            if (thresholdCriteria.comparator.compare(measure.value(), threshold) >= 0) EvaluationResult.PASS else EvaluationResult.FAIL
        val builder = unrecoverable().result(result)
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(
                String.format(
                    thresholdCriteria.failMessageTemplate, measureName, measure.value(), measure.unit(), threshold
                )
            ).addFailGeneralMessages(generalMessage(measureName.name))
        } else {
            builder.addPassSpecificMessages(
                String.format(
                    thresholdCriteria.passMessageTemplate, measureName, measure.value(), measure.unit(), threshold
                )
            ).addPassGeneralMessages(generalMessage(measureName.name))
        }
        return builder.build()
    }

    companion object {
        private fun generalMessage(measureName: String): String {
            return String.format("%s requirements", measureName)
        }
    }
}