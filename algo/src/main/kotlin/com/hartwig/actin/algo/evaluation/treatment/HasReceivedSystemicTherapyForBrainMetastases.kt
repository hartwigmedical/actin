package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasReceivedSystemicTherapyForBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val cnsOrBrainMetastases =
            record.tumor.hasConfirmedOrSuspectedCnsLesions() == true || record.tumor.hasConfirmedOrSuspectedBrainLesions() == true
        val hasHadSystemicTreatment = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory) > 0

        return if (cnsOrBrainMetastases && hasHadSystemicTreatment) {
            EvaluationFactory.warn(
                "Patient has possibly received systemic therapy for brain metastases",
                "Has possibly received systemic therapy for brain metastases"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received systemic therapy for brain metastases",
                "Has not received systemic therapy for brain metastases"
            )
        }
    }
}