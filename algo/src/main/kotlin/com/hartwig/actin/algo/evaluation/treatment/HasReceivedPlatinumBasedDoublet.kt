package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasReceivedPlatinumBasedDoublet : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val message = "received platinum based doublet chemotherapy"

        return when {
            receivedPlatinumDoublet(record) -> {
                EvaluationFactory.pass("Patient has $message", "Has $message ")
            }

            receivedPlatinumTripletOrAbove(record) -> {
                EvaluationFactory.warn(
                    "Patient has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)",
                    "Has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not $message", "Has not $message")
            }
        }
    }

    private fun receivedPlatinumDoublet(record: PatientRecord): Boolean {

        return record.clinical.oncologicalHistory.any { entry ->
            val treatments = entry.allTreatments()

            treatments.filter { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }.size == 2
                    && treatments.any { treatment ->
                treatment.types().contains(DrugType.PLATINUM_COMPOUND) &&
                        (treatments - treatment).any { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }
            }
        }
    }

    private fun receivedPlatinumTripletOrAbove(record: PatientRecord): Boolean {

        return record.clinical.oncologicalHistory.any { entry ->
            val treatments = entry.allTreatments()
            treatments.filter { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }.size > 2
                    && treatments.any { treatment ->
                treatment.types().contains(DrugType.PLATINUM_COMPOUND) &&
                        (treatments - treatment).any { it.categories().contains(TreatmentCategory.CHEMOTHERAPY) }
            }
        }
    }
}
