package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadSomeSystemicTreatments(private val minSystemicTreatments: Int, private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.oncologicalHistory)
        return when {
            minSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.pass(labels.hasHadSomeSystemicTreatmentsPass(minSystemicTreatments))
            }

            maxSystemicCount >= minSystemicTreatments -> {
                EvaluationFactory.undetermined(labels.hasHadSomeSystemicTreatmentsUndetermined(minSystemicTreatments))
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadSomeSystemicTreatmentsFail(minSystemicTreatments))
            }
        }
    }
}