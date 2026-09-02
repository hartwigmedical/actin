package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadRadiotherapyToSomeBodyLocation(private val bodyLocation: String, private val lines: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorRadiotherapies = record.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }

        val radiotherapyToTargetLocationCount =
            priorRadiotherapies.count { radiotherapy ->
                radiotherapy.treatmentHistoryDetails?.bodyLocations?.any { it.lowercase().contains(bodyLocation.lowercase()) } == true
            }

        val messageEnding = lines?.let { " for at least $it lines" } ?: ""

        return when {
            radiotherapyToTargetLocationCount >= (lines ?: 1) -> {
                EvaluationFactory.pass("Prior radiotherapy to $bodyLocation$messageEnding in provided treatments")
            }

            priorRadiotherapies.any { it.treatmentHistoryDetails?.bodyLocations == null } -> {
                EvaluationFactory.recoverableUndetermined("Radiotherapy in provided treatments but undetermined if target location was $bodyLocation")
            }

            else -> {
                EvaluationFactory.fail("No prior radiation therapy to $bodyLocation$messageEnding in provided treatments")
            }
        }
    }
}