package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadRadiotherapyToSomeBodyLocation(private val bodyLocation: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorRadiotherapies = record.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }

        val hadRadiotherapyToTargetLocation =
            priorRadiotherapies.any { radiotherapy ->
                radiotherapy.treatmentHistoryDetails?.bodyLocations?.any { it.lowercase().contains(bodyLocation.lowercase()) } == true
            }

        val message = "prior radiotherapy to $bodyLocation"

        return when {
            hadRadiotherapyToTargetLocation -> {
                EvaluationFactory.pass("Has had $message")
            }

            priorRadiotherapies.any { it.treatmentHistoryDetails?.bodyLocations == null } -> {
                EvaluationFactory.recoverableUndetermined("Has received radiotherapy but undetermined if target location was $bodyLocation - assuming not")
            }

            else -> {
                EvaluationFactory.fail("Has not received prior radiation therapy to $bodyLocation")
            }
        }
    }
}