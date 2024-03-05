package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadLiverResection : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSurgeries = record.clinical.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.SURGERY) }

        val hadResection = priorSurgeries.filter { it.treatments.any { treatment -> treatment.name.contains(RESECTION_KEYWORD) } }
        val hadResectionToTargetLocation =
            hadResection.any { it.treatmentHistoryDetails?.bodyLocations?.any { it.lowercase().contains("liver") } == true }
        val hadResectionToUnknownLocation = hadResection.any { it.treatmentHistoryDetails?.bodyLocations == null }

        val hadSurgeryWithUnknownName = priorSurgeries.filter {
            it.treatments.any {
                it.name.equals(
                    "Surgery",
                    true
                )
            }
        }
        val hadSurgeryWithUnknownNamePotentiallyToTargetLocation = hadSurgeryWithUnknownName.any {
            it.treatmentHistoryDetails?.bodyLocations?.any {
                it.lowercase().contains("liver")
            } == true || it.treatmentHistoryDetails?.bodyLocations == null
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