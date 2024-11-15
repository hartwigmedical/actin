package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasReceivedSystemicTherapyForBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val confirmedCnsOrBrainMetastases = tumorDetails.hasConfirmedBrainLesions() || tumorDetails.hasConfirmedCnsLesions()
        val suspectedCnsOrBrainMetastases = tumorDetails.hasSuspectedCnsLesions == true || tumorDetails.hasSuspectedBrainLesions == true
        val hasHadSystemicTreatment = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory) > 0

        return if ((confirmedCnsOrBrainMetastases || suspectedCnsOrBrainMetastases) && hasHadSystemicTreatment) {
            val suspectedMessage = if (!confirmedCnsOrBrainMetastases) " (suspected)" else ""
            EvaluationFactory.warn(
                "Patient has possibly received systemic therapy for$suspectedMessage brain metastases",
                "Has possibly received systemic therapy for$suspectedMessage brain metastases"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received systemic therapy for brain metastases",
                "Has not received systemic therapy for brain metastases"
            )
        }
    }
}