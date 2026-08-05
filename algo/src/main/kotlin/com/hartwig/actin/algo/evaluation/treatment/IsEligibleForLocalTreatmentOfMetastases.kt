package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class IsEligibleForLocalTreatmentOfMetastases(
    private val hasMetastaticCancer: HasMetastaticCancer,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail(labels.isEligibleForLocalTreatmentOfMetastasesFail())
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(labels.isEligibleForLocalTreatmentOfMetastasesUndetermined())
            }

            else -> {
                EvaluationFactory.undetermined(labels.isEligibleForLocalTreatmentOfMetastasesUndeterminedMetastaticUnknown())
            }
        }
    }
}