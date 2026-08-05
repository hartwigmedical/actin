package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadBrainRadiationTherapy(private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val hasConfirmedBrainOrCNSMetastases = tumorDetails.hasConfirmedBrainLesions() || tumorDetails.hasConfirmedCnsLesions()
        val hasSuspectedBrainOrCNSMetastases = tumorDetails.hasSuspectedBrainLesions == true || tumorDetails.hasSuspectedCnsLesions == true
        val priorRadiotherapies = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
        val anyRadiotherapy = priorRadiotherapies.isNotEmpty()
        val brainRadiotherapy = hasHadBrainRadiotherapy(priorRadiotherapies)

        return when {
            brainRadiotherapy == true -> EvaluationFactory.pass(labels.hasHadBrainRadiationTherapyPass())

            brainRadiotherapy == false && anyRadiotherapy -> EvaluationFactory.fail(labels.hasHadBrainRadiationTherapyFailNotToBrain())

            (hasConfirmedBrainOrCNSMetastases || hasSuspectedBrainOrCNSMetastases) && anyRadiotherapy -> {
                val suspectedSuffix = if (!hasConfirmedBrainOrCNSMetastases) labels.hasHadBrainRadiationTherapySuspectedSuffix() else ""
                EvaluationFactory.undetermined(labels.hasHadBrainRadiationTherapyUndetermined(suspectedSuffix))
            }

            else -> EvaluationFactory.fail(labels.hasHadBrainRadiationTherapyFail())
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
