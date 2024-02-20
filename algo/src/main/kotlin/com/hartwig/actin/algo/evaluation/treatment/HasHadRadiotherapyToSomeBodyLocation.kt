package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadRadiotherapyToSomeBodyLocation(private val bodyLocation: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorRadiotherapy = record.clinical.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
        val radiotherapyToTargetLocation =
            priorRadiotherapy.any {
                it.treatmentHistoryDetails?.bodyLocations?.let { location ->
                    stringCaseInsensitivelyMatchesQueryCollection(
                        bodyLocation,
                        location
                    )
                } == true
            }

        val message = "prior radiotherapy to $bodyLocation"

        return when {
            radiotherapyToTargetLocation -> {
                EvaluationFactory.pass(
                    "Patient has had $message", "Has had $message"
                )
            }

            priorRadiotherapy.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Patient has received radiotherapy but undetermined if target location was $bodyLocation",
                    "Undetermined prior $bodyLocation radiation therapy"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received prior radiation therapy to $bodyLocation",
                    "Has not received prior radiation therapy to $bodyLocation"
                )
            }
        }
    }
}