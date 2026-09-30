package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadOncologicalSurgeryInSpecificBodyLocation(private val bodyLocations: Set<BodyLocationCategory>) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val surgeries = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.SURGERY) }
        val surgeriesInTargetLocation =
            surgeries.filter { it.treatmentHistoryDetails?.bodyLocationCategories?.any(bodyLocations::contains) == true }

        val locationString = Format.concatItemsWithOr(bodyLocations)

        return when {
            surgeriesInTargetLocation.isNotEmpty() -> {
                val locations =
                    surgeriesInTargetLocation.flatMap { it.treatmentHistoryDetails?.bodyLocationCategories ?: emptySet() }.toSet()
                EvaluationFactory.pass(
                    "Oncological surgery in location(s) " + Format.concatItemsWithAnd(locations) + " in provided treatments"
                )
            }

            surgeries.any { it.treatmentHistoryDetails?.bodyLocationCategories == null } -> {
                EvaluationFactory.undetermined(
                    "Oncological surgery in provided treatments but undetermined if in location(s) $locationString"
                )
            }

            else -> {
                EvaluationFactory.fail("No oncological surgery in location(s) $locationString in provided treatments")
            }
        }
    }
}