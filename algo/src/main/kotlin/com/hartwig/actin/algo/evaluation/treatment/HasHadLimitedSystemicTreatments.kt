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
                EvaluationFactory.pass("At most $maxSystemicTreatments systemic treatments in provided treatments")
            }

            minSystemicCount <= maxSystemicTreatments -> {
                EvaluationFactory.undetermined("Undetermined if provided treatments include more than $maxSystemicTreatments systemic treatments")
            }

            else -> {
                EvaluationFactory.fail("More than $maxSystemicTreatments systemic treatments in provided treatments")
            }
        }
    }
}