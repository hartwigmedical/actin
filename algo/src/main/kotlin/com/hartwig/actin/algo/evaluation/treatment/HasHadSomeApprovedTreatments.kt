package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHadSomeApprovedTreatments(private val minApprovedTreatments: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.oncologicalHistory.isEmpty() && minApprovedTreatments > 0) {
            EvaluationFactory.fail(
                "Patient has not had prior tumor treatment, and thus no approved treatments",
                "Has not had approved treatments"
            )
        } else
            EvaluationFactory.undetermined(
                "Currently the number of approved treatments cannot be determined",
                "Undetermined nr of approved treatments"
            )
    }
}