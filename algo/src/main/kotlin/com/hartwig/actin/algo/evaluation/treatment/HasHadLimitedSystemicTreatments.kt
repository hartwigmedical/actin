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
                EvaluationFactory.pass("Has received at most $maxSystemicTreatments systemic treatments")
            }

            minSystemicCount <= maxSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined if received more than $maxSystemicTreatments systemic treatments")
            }

            else -> {
                EvaluationFactory.fail("Has received more than $maxSystemicTreatments systemic treatments")
            }
        }
    }
}