package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadOncologicalSurgeryInSpecificBodyLocation(private val bodyLocations: Set<BodyLocationCategory>): EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val surgeries = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.SURGERY) }
        val surgeriesInTargetLocation =
            surgeries.filter { it.treatmentHistoryDetails?.bodyLocationCategories?.any { location -> location in bodyLocations } == true }

        val locationString = if (surgeriesInTargetLocation.isNotEmpty()) {
            surgeriesInTargetLocation.joinToString(", ") { it.treatmentHistoryDetails?.bodyLocationCategories?.joinToString(", ") ?: "" }
        } else {
            Format.concatItemsWithOr(bodyLocations)
        }

        return when {
            surgeriesInTargetLocation.isNotEmpty() -> {
                EvaluationFactory.pass("Has had oncological surgery in location(s) $locationString")
            }

            surgeries.any { it.treatmentHistoryDetails?.bodyLocationCategories == null } -> {
                EvaluationFactory.undetermined("Has received oncological surgery but undetermined if in location(s) $locationString")
            }

            else -> {
                EvaluationFactory.fail("Has not received oncological surgery in location(s) $locationString")
            }
        }
    }
}