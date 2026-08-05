package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadRadiotherapyToSomeBodyLocation(
    private val bodyLocation: String,
    private val lines: Int?,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorRadiotherapies = record.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }

        val radiotherapyToTargetLocationCount =
            priorRadiotherapies.count { radiotherapy ->
                radiotherapy.treatmentHistoryDetails?.bodyLocations?.any { it.lowercase().contains(bodyLocation.lowercase()) } == true
            }

        val messageEnding = lines?.let { labels.hasHadRadiotherapyToSomeBodyLocationSuffixForAtLeastLines(it) } ?: ""

        return when {
            radiotherapyToTargetLocationCount >= (lines ?: 1) -> {
                EvaluationFactory.pass(labels.hasHadRadiotherapyToSomeBodyLocationPass(bodyLocation, messageEnding))
            }

            priorRadiotherapies.any { it.treatmentHistoryDetails?.bodyLocations == null } -> {
                EvaluationFactory.recoverableUndetermined(labels.hasHadRadiotherapyToSomeBodyLocationRecoverableUndetermined(bodyLocation))
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadRadiotherapyToSomeBodyLocationFail(bodyLocation, messageEnding))
            }
        }
    }
}
