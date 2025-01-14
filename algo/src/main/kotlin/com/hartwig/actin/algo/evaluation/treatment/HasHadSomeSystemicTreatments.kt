package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadSomeSystemicTreatments(private val minSystemicTreatments: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.oncologicalHistory)
        return when {
            minSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.pass("Received at least $minSystemicTreatments systemic treatments")
            }

            maxSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined if received at least $minSystemicTreatments systemic treatments")
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minSystemicTreatments systemic treatments")
            }
        }
    }
}