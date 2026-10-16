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
                EvaluationFactory.pass("At least $minSystemicTreatments systemic treatments in provided treatments")
            }

            maxSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined history of at least $minSystemicTreatments systemic treatments based on provided treatments")
            }

            else -> {
                EvaluationFactory.fail("Not at least $minSystemicTreatments systemic treatments in provided treatments")
            }
        }
    }
}