package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasCardiacArrhythmia internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val ecg = record.clinical().clinicalStatus().ecg()
            ?: return EvaluationFactory.fail(
                "ECG details are missing, it is assumed there are no abnormalities",
                "Assumed no ECG abnormalities"
            )
        return when {
            ecg.hasSigAberrationLatestECG() && ecg.aberrationDescription() != null -> {
                EvaluationFactory.pass(
                    "Patient has known ECG abnormalities: " + ecg.aberrationDescription(),
                    "Present ECG abnormalities: " + ecg.aberrationDescription()
                )
            }

            ecg.hasSigAberrationLatestECG() -> {
                EvaluationFactory.pass(
                    "Patient has present ECG abnormalities, but aberration details unknown",
                    "Present ECG abnormalities (details unknown)"
                )
            }

            else ->
                EvaluationFactory.fail("Patient has no known ECG abnormalities", "No known ECG abnormalities")
        }
    }
}