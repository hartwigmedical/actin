package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

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
                EvaluationFactory.pass("Had had liver resection")
            }

            hadResectionToUnknownLocation || hadSurgeryWithUnknownNamePotentiallyToTargetLocation -> {
                EvaluationFactory.undetermined("Undetermined if received surgery was liver resection")
            }

            else -> {
                EvaluationFactory.fail("Has not had liver resection")
            }
        }
    }

    companion object {
        const val RESECTION_KEYWORD = "resection"
    }
}