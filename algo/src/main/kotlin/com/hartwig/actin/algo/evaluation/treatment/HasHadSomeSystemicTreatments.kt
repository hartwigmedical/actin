package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHadSomeSystemicTreatments internal constructor(private val minSystemicTreatments: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.clinical().priorTumorTreatments())
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.clinical().priorTumorTreatments())
        return when {
            minSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.pass(
                    "Patient received at least $minSystemicTreatments systemic treatments",
                    "Received at least $minSystemicTreatments systemic treatments"
                )
            }

            maxSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.undetermined(
                    "Could not determine if patient received at least $minSystemicTreatments systemic treatments",
                    "Undetermined if received at least $minSystemicTreatments systemic treatments"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient did not receive at least $minSystemicTreatments systemic treatments",
                    "Has not received at least $minSystemicTreatments systemic treatments"
                )
            }
        }
    }
}