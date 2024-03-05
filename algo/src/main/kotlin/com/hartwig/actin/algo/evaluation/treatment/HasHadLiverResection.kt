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

        val hadSurgeryToTargetLocation =
            priorSurgeries.any {
                it.treatmentHistoryDetails?.bodyLocations?.any { it.lowercase().contains("liver") } == true
            }

        return when {
            hadSurgeryToTargetLocation && priorSurgeries.any { it.treatments.any { treatment -> treatment.name.contains(RESECTION_KEYWORD) } } -> {
                EvaluationFactory.pass("Patient has had a liver resection", "Had had liver resection")
            }

            priorSurgeries.any {
                it.treatmentHistoryDetails?.bodyLocations == null && it.treatments.any { treatment ->
                    treatment.name.contains(
                        RESECTION_KEYWORD
                    )
                }
            } || priorSurgeries.any { it.treatments.any { it.name.isEmpty() } }

            -> {
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