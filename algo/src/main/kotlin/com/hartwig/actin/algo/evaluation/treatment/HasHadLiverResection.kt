package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadLiverResection : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSurgeries = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.SURGERY) }

        val priorResections = priorSurgeries.filter { it.treatments.any { treatment -> treatment.name.contains(RESECTION_KEYWORD) } }
        val hadResectionToTargetLocation =
            priorResections.any { it.treatmentHistoryDetails?.bodyLocationCategories?.any { location -> location == BodyLocationCategory.LIVER } == true }
        val hadResectionToUnknownLocation = priorResections.any { it.treatmentHistoryDetails?.bodyLocationCategories == null }

        val hadSurgeryWithUnknownNamePotentiallyToTargetLocation = priorSurgeries.any {
            it.treatments.any { treatment ->
                treatment.name.equals(
                    "Surgery",
                    true
                )
            } && it.treatmentHistoryDetails?.bodyLocationCategories?.any { category -> category == BodyLocationCategory.LIVER } != false
        }

        return when {
            hadResectionToTargetLocation -> {
                EvaluationFactory.pass("Patient has had a liver resection", "Had had liver resection")
            }

            hadResectionToUnknownLocation || hadSurgeryWithUnknownNamePotentiallyToTargetLocation -> {
                EvaluationFactory.undetermined(
                    "Undetermined if surgery performed was a liver resection",
                    "Undetermined if surgery was liver resection"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not had a liver resection", "Has not had liver resection")
            }
        }
    }

    companion object {
        const val RESECTION_KEYWORD = "resection"
    }
}