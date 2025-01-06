package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class IsEligibleForLocalTreatmentOfMetastases(private val hasMetastaticCancer: HasMetastaticCancer) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail("No metastatic cancer and hence no eligibility for local treatment of metastases")
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.undetermined("Eligibility for local treatment of metastases undetermined")
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined if metastatic cancer and therefore undetermined eligibility for local treatment of metastases")
            }
        }
    }
}