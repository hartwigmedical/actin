package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.tumor.hasConfirmedOrSuspectedLiverLesions()
        val hasCnsMetastases = record.tumor.hasConfirmedOrSuspectedCnsLesions()
        val hasBrainMetastases = record.tumor.hasConfirmedOrSuspectedBrainLesions()
        val hasBoneLesions = record.tumor.hasConfirmedOrSuspectedBoneLesions()
        val hasLungLesions = record.tumor.hasConfirmedOrSuspectedLungLesions()
        val hasLymphNodeLesions = record.tumor.hasConfirmedOrSuspectedLymphNodeLesions()
        val otherLesions = record.tumor.otherConfirmedOrSuspectedLesions()
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