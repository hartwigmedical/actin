package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class MeetsSpecificCriteriaRegardingLiverMetastases internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Study specific criteria regarding liver metastases are currently not determined yet",
            "Undetermined if study specific criteria regarding liver metastases are met"
        )
    }
}