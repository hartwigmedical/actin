package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class MeetsSpecificCriteriaRegardingLiverMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.clinical().tumor().hasLiverLesions()
            ?: return EvaluationFactory.undetermined(
                "Unknown if patient has liver lesions hence undetermined if study specific criteria regarding liver metastases are met",
                "Undetermined if study specific criteria regarding liver metastases are met"
            )
        return if (hasLiverMetastases) {
            EvaluationFactory.undetermined(
                "Patient has liver lesions but undetermined if these meet the specific protocol criteria",
                "Undetermined if study specific criteria regarding liver metastases are met"
            )
        } else {
            EvaluationFactory.fail(
                "No liver metastases present hence also won't meet specific protocol criteria regarding brain metastases",
                "Specific criteria regarding liver metastases not met"
            )
        }
    }
}