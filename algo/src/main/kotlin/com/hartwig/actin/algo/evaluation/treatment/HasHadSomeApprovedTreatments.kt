package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadSomeApprovedTreatments(
    private val minApprovedTreatments: Int,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.oncologicalHistory.isEmpty() && minApprovedTreatments > 0) {
            EvaluationFactory.fail(labels.hasHadSomeApprovedTreatmentsFail())
        } else
            EvaluationFactory.undetermined(labels.hasHadSomeApprovedTreatmentsUndetermined())
    }
}
