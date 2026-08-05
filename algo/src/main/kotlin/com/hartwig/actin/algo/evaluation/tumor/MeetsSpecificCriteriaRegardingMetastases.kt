package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class MeetsSpecificCriteriaRegardingMetastases(
    private val hasMetastaticCancer: HasMetastaticCancer,
    private val labels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail(labels.meetsSpecificCriteriaRegardingMetastasesFail())
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingMetastasesUndetermined())
            }

            else -> {
                EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingMetastasesUndeterminedUnknown())
            }
        }
    }
}