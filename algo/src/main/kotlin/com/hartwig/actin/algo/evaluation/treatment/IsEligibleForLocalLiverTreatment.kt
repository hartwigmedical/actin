package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.HasLiverMetastases

class IsEligibleForLocalLiverTreatment(private val hasLiverMetastases: HasLiverMetastases) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasLiverMetastases.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail(
                    "Patient has no liver metastases and is hence not eligible for local liver treatment",
                    "No liver metastases (hence no eligibility for local liver treatment)"
                )
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(
                    "Undetermined if liver metastases are eligible for local liver treatment",
                    "Undetermined eligibility for local liver treatment"
                )
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has liver metastases and therefore undetermined if patient is eligible for local liver treatment",
                    "Undetermined liver metastases and therefore undetermined eligibility for local liver treatment"
                )
            }
        }
    }
}