package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val hasLiverMetastases = tumorDetails.hasLiverLesions
        val hasCnsMetastases = tumorDetails.hasConfirmedCnsLesions()
        val hasBrainMetastases = tumorDetails.hasConfirmedBrainLesions()
        val hasBoneLesions = tumorDetails.hasBoneLesions
        val hasLungLesions = tumorDetails.hasLungLesions
        val hasLymphNodeLesions = tumorDetails.hasLymphNodeLesions
        val otherLesions = tumorDetails.otherLesions
        val hasSuspectedLesions = tumorDetails.hasSuspectedLesions()

        if (listOf(
                hasLiverMetastases,
                hasCnsMetastases,
                hasBrainMetastases,
                hasBoneLesions,
                hasLungLesions,
                hasLymphNodeLesions,
                otherLesions
            ).all { it == null } && !hasSuspectedLesions
        ) {
            return EvaluationFactory.undetermined("Data about lesions is missing", "Missing lesions details")
        }

        val hasOtherLesions = !otherLesions.isNullOrEmpty()
        val hasLesions = listOf(
            hasLiverMetastases, hasCnsMetastases, hasBrainMetastases, hasBoneLesions, hasLungLesions, hasLymphNodeLesions, hasOtherLesions
        ).any { it == true }

        return when {
            hasLesions -> {
                EvaluationFactory.pass("Patient has at least one lesion", "Lesions present")
            }
            hasSuspectedLesions -> {
                EvaluationFactory.undetermined(
                    "Undetermined if any lesions present (suspected lesions only)",
                    "Undetermined if any lesions present (suspected lesions only)"
                )
            }
            else -> {
                EvaluationFactory.fail("Patient does not have any lesions", "No lesions present")
            }
        }
    }
}