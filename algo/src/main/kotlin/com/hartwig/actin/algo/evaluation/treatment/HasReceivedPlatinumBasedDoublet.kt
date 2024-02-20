package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasReceivedPlatinumBasedDoublet : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val chemotherapyTreatments = record.clinical.oncologicalHistory
            .filter { it.allTreatments().size > 1 }
            .flatMap(TreatmentHistoryEntry::allTreatments)
            .filter { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }

        val receivedPlatinumDoublet = chemotherapyTreatments.any { treatment ->
            treatment.types().contains(DrugType.PLATINUM_COMPOUND) && chemotherapyTreatments.any { otherTreatment ->
                otherTreatment != treatment
            }
        }

        val message = "received platinum based doublet chemotherapy"

        return when {
            receivedPlatinumDoublet -> {
                EvaluationFactory.pass("Patient has $message", "Has $message ")
            }

            else -> {
                EvaluationFactory.fail("Patient has not $message", "Has not $message")
            }
        }
    }
}