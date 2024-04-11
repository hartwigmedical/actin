package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHadSomeSystemicTreatments(private val minSystemicTreatments: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.oncologicalHistory)
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