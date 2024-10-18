package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasBrainMetastases = record.tumor.hasConfirmedOrSuspectedBrainLesions()
        val hasActiveBrainMetastases = record.tumor.hasActiveBrainLesions
        val hasCNSLesions = record.tumor.hasConfirmedOrSuspectedCnsLesions()

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
                "No known brain metastases"
            )
        }
    }
}