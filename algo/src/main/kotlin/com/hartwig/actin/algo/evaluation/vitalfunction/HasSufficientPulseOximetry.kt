package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory

class HasSufficientPulseOximetry internal constructor(private val minMedianPulseOximetry: Double) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val pulseOximetries = VitalFunctionSelector.selectMedianPerDay(
            record.clinical().vitalFunctions(), VitalFunctionCategory.SPO2,
            null, MAX_PULSE_OXIMETRY_TO_USE
        )

        if (pulseOximetries.isEmpty()) {
            return recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("No pulse oximetries readouts found")
                .build()
        }
        val median = VitalFunctionFunctions.determineMedianValue(pulseOximetries)
        return if (median.compareTo(minMedianPulseOximetry) >= 0) {
            EvaluationFactory.recoverablePass(
                "Patient has median pulse oximetry exceeding $minMedianPulseOximetry",
                "Pulse oximetry above $minMedianPulseOximetry"
            )
        } else if (pulseOximetries.any { it.value().compareTo(minMedianPulseOximetry) >= 0 }) {
            return EvaluationFactory.recoverableUndetermined(
                "Patient has median pulse oximetry ($median%) below $minMedianPulseOximetry but also at least one "
                        + "measure above $minMedianPulseOximetry", "Median pulse oximetry ($median%) below requirements"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient has median pulse oximetry ($median%) below $minMedianPulseOximetry",
                "Median pulse oximetry ($median%) below $minMedianPulseOximetry"
            )
        }
    }

    companion object {
        private const val MAX_PULSE_OXIMETRY_TO_USE = 5
    }
}