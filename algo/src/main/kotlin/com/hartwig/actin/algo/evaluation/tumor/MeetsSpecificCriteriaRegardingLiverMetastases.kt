package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingLiverMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.tumor.hasLiverLesions
            ?: return EvaluationFactory.undetermined("Liver lesions undetermined - hence undetermined if study specific criteria regarding liver metastases are met")
        return if (hasLiverMetastases) {
            EvaluationFactory.undetermined("Undetermined if study specific criteria regarding liver metastases are met")
        } else {
            EvaluationFactory.fail("No liver metastases hence won't meet study specific criteria regarding liver metastases")
        }
    }
}