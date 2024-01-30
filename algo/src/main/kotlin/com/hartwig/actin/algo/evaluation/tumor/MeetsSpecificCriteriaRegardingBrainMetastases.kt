package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class MeetsSpecificCriteriaRegardingBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasBrainMetastases = record.clinical.tumor.hasBrainLesions
        val hasActiveBrainMetastases = record.clinical.tumor.hasActiveBrainLesions
        val hasCNSLesions = record.clinical.tumor.hasCnsLesions

        // We assume that if a patient has active brain metastases, hasBrainMetastases is allowed to be (theoretically) null/false
        return if (hasActiveBrainMetastases == true) {
            EvaluationFactory.undetermined(
                "Patient has brain metastases that are considered active, undetermined if these meet the specific protocol criteria",
                "Undetermined if study specific criteria regarding brain metastases are met"
            )
        } else if (hasBrainMetastases == true) {
            EvaluationFactory.undetermined(
                "Patient has brain metastases, undetermined if these meet the specific protocol criteria",
                "Undetermined if study specific criteria regarding brain metastases are met"
            )
        } else if (hasCNSLesions == true && hasBrainMetastases == null) {
            EvaluationFactory.undetermined(
                "Patient has CNS metastases, undetermined if patient also has brain metastases and if these meet the specific protocol criteria",
                "Undetermined if study specific criteria regarding brain metastases are met"
            )
        } else {
            EvaluationFactory.fail(
                "No known brain metastases present hence also won't meet specific protocol criteria regarding brain metastases",
                "Specific criteria regarding brain metastases not met"
            )
        }
    }
}