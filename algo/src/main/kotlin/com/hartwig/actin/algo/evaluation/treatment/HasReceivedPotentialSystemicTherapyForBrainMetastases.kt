package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.Treatment

class HasReceivedPotentialSystemicTherapyForBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val cnsOrBrainMetastases = record.clinical.tumor.hasCnsLesions == true || record.clinical.tumor.hasBrainLesions == true
        val hasHadSystemicTreatment = record.clinical.oncologicalHistory.filter { it.allTreatments().any(Treatment::isSystemic) }

        if (cnsOrBrainMetastases && hasHadSystemicTreatment.isNotEmpty()) {
            return EvaluationFactory.warn(
                "Patient has possibly received systemic therapy for brain metastases",
                "Has possibly received systemic therapy for brain metastases"
            )
        } else {
            return EvaluationFactory.fail(
                "Patient has not received systemic therapy for brain metastases",
                "Has not received systemic therapy for brain metastases"
            )
        }
    }
}