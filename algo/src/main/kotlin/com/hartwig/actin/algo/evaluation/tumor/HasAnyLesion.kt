package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.tumor.hasLiverLesions
        val hasCnsMetastases = record.tumor.hasCnsLesions
        val hasBrainMetastases = record.tumor.hasBrainLesions
        val hasBoneLesions = record.tumor.hasBoneLesions
        val hasLungLesions = record.tumor.hasLungLesions
        val hasLymphNodeLesions = record.tumor.hasLymphNodeLesions
        val otherLesions = record.tumor.otherLesions
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