package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadBrainRadiationTherapy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val hasConfirmedBrainOrCNSMetastases = tumorDetails.hasConfirmedBrainLesions() || tumorDetails.hasConfirmedCnsLesions()
        val hasSuspectedBrainOrCNSMetastases = tumorDetails.hasSuspectedBrainLesions == true || tumorDetails.hasSuspectedCnsLesions == true
        val priorRadiotherapies = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
        val anyRadiotherapy = priorRadiotherapies.isNotEmpty()
        val brainRadiotherapy = hasHadBrainRadiotherapy(priorRadiotherapies)

        return when {
            brainRadiotherapy == true -> EvaluationFactory.pass(
                "Patient has had prior brain radiation therapy",
                "Has had brain radiation therapy"
            )

            brainRadiotherapy == false && anyRadiotherapy -> EvaluationFactory.fail(
                "Patient has received radiotherapy but not to the brain",
                "Has not received brain radiation therapy"
            )

            (hasConfirmedBrainOrCNSMetastases || hasSuspectedBrainOrCNSMetastases) && anyRadiotherapy -> {
                val suspectedMessage = if (!hasConfirmedBrainOrCNSMetastases) " suspected" else ""
                EvaluationFactory.undetermined(
                    "Patient has$suspectedMessage brain and/or CNS metastases and has received radiotherapy but undetermined if brain radiation therapy",
                    "Undetermined prior brain radiation therapy"
                )
            }

            else -> EvaluationFactory.fail(
                "Patient has not received prior brain radiation therapy",
                "Has not received prior brain radiation therapy"
            )
        }
    }

    private fun hasHadBrainRadiotherapy(priorRadiotherapyEntries: List<TreatmentHistoryEntry>): Boolean? {
        val brainOrCnsLocations = setOf(BodyLocationCategory.BRAIN, BodyLocationCategory.CNS)

        val radiotherapyEvaluations = priorRadiotherapyEntries.map { entry ->
            entry.treatmentHistoryDetails?.let { details ->
                val hasBrainOrCnsLocation = details.bodyLocationCategories
                    ?.intersect(brainOrCnsLocations)
                    ?.isNotEmpty() ?: false

                val hasSpinalLocation = details.bodyLocations?.any {
                    stringCaseInsensitivelyMatchesQueryCollection(it, listOf("spine", "spinal"))
                } ?: false

                hasBrainOrCnsLocation && !hasSpinalLocation
            }
        }.toSet()
        return when {
            true in radiotherapyEvaluations -> true
            null in radiotherapyEvaluations -> null
            else -> false
        }
    }
}
