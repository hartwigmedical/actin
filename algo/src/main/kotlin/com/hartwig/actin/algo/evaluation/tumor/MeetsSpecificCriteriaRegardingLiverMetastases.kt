package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingLiverMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.tumor.hasLiverLesions
            ?: return EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingLiverMetastasesUndeterminedMissing())
        return if (hasLiverMetastases) {
            EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingLiverMetastasesUndetermined())
        } else {
            EvaluationFactory.fail(labels.meetsSpecificCriteriaRegardingLiverMetastasesFail())
        }
    }
}