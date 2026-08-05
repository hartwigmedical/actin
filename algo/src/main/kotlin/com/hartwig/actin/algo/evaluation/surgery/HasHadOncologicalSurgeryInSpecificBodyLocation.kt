package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadOncologicalSurgeryInSpecificBodyLocation(
    private val bodyLocations: Set<BodyLocationCategory>,
    private val labels: EvaluationLabels.Surgery
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val surgeries = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.SURGERY) }
        val surgeriesInTargetLocation =
            surgeries.filter { it.treatmentHistoryDetails?.bodyLocationCategories?.any(bodyLocations::contains) == true }

        val locationString = Format.concatItemsWithOr(bodyLocations)

        return when {
            surgeriesInTargetLocation.isNotEmpty() -> {
                val locations =
                    surgeriesInTargetLocation.flatMap { it.treatmentHistoryDetails?.bodyLocationCategories ?: emptySet() }.toSet()
                EvaluationFactory.pass(labels.hasHadOncologicalSurgeryInSpecificBodyLocationPass(Format.concatItemsWithAnd(locations)))
            }

            surgeries.any { it.treatmentHistoryDetails?.bodyLocationCategories == null } -> {
                EvaluationFactory.undetermined(labels.hasHadOncologicalSurgeryInSpecificBodyLocationUndetermined(locationString))
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadOncologicalSurgeryInSpecificBodyLocationFail(locationString))
            }
        }
    }
}