package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer

class IsEligibleForLocalTreatmentOfMetastases(private val hasMetastaticCancer: HasMetastaticCancer) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail(
                    "Patient has no metastatic cancer and is hence not eligible for local treatment of metastases",
                    "No metastatic cancer and hence no eligibility for local treatment of metastases"
                )
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(
                    "Undetermined if metastases are eligible for local treatment of metastases",
                    "Undetermined eligibility for local treatment of metastases"
                )
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has metastatic cancer and therefore undetermined if patient is eligible for local treatment of metastases",
                    "Undetermined if metastatic cancer and therefore undetermined eligibility for local treatment of metastases"
                )
            }
        }
    }
}