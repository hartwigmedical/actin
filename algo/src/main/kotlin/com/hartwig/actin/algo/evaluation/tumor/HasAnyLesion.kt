package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasAnyLesion internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.clinical().tumor().hasLiverLesions()
        val hasCnsMetastases = record.clinical().tumor().hasCnsLesions()
        val hasBrainMetastases = record.clinical().tumor().hasBrainLesions()
        val hasBoneLesions = record.clinical().tumor().hasBoneLesions()
        val hasLungLesions = record.clinical().tumor().hasLungLesions()
        val hasLymphNodeLesions = record.clinical().tumor().hasLymphNodeLesions()
        val otherLesions = record.clinical().tumor().otherLesions()
        if (listOf(
                hasLiverMetastases,
                hasCnsMetastases,
                hasBrainMetastases,
                hasBoneLesions,
                hasLungLesions,
                hasLymphNodeLesions,
                otherLesions
            ).all { it == null }
        ) {
            return EvaluationFactory.undetermined("Data about lesions is missing", "Missing lesions details")
        }
        val hasOtherLesions = !otherLesions.isNullOrEmpty()
        val hasLesions = listOf(
            hasLiverMetastases, hasCnsMetastases, hasBrainMetastases, hasBoneLesions, hasLungLesions, hasLymphNodeLesions, hasOtherLesions
        ).any { it == true }

        return if (hasLesions) {
            EvaluationFactory.pass("Patient has at least one lesion", "Lesions present")
        } else {
            EvaluationFactory.fail("Patient does not have any lesions", "No lesions present")
        }
    }
}