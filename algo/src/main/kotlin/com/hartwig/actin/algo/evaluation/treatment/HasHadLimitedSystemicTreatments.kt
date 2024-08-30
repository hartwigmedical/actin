package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadLimitedSystemicTreatments(private val maxSystemicTreatments: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.oncologicalHistory)
        return when {
            maxSystemicCount <= maxSystemicTreatments -> {
                EvaluationFactory.pass(
                    "Patient has received at most $maxSystemicTreatments systemic treatments",
                    "Has received at most $maxSystemicTreatments systemic treatments"
                )
            }

            minSystemicCount <= maxSystemicTreatments -> {
                EvaluationFactory.undetermined(
                    "Could not determine if patient received at most $maxSystemicTreatments systemic treatments",
                    "Undetermined if received more than $maxSystemicTreatments systemic treatments"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has received more than $maxSystemicTreatments systemic treatments",
                    "Has received more than $maxSystemicTreatments systemic treatments"
                )
            }
        }
    }
}