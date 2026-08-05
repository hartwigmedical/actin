package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadLimitedSystemicTreatments(
    private val maxSystemicTreatments: Int,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory)
        val maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.oncologicalHistory)
        return when {
            maxSystemicCount <= maxSystemicTreatments -> {
                EvaluationFactory.pass(labels.hasHadLimitedSystemicTreatmentsPass(maxSystemicTreatments))
            }

            minSystemicCount <= maxSystemicTreatments -> {
                EvaluationFactory.undetermined(labels.hasHadLimitedSystemicTreatmentsUndetermined(maxSystemicTreatments))
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadLimitedSystemicTreatmentsFail(maxSystemicTreatments))
            }
        }
    }
}