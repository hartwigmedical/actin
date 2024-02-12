package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasReceivedPotentialSystemicTherapyForBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val cnsOrBrainMetastases = record.clinical.tumor.hasCnsLesions == true || record.clinical.tumor.hasBrainLesions == true
        val hasHadSystemicTreatment = SystemicTreatmentAnalyser.minSystemicTreatments(record.clinical.oncologicalHistory) > 0

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