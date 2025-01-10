package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory

class HasMinimumLesionsInSpecificBodyLocation(
    private val minimumLesions: Int, private val bodyLocation: BodyLocationCategory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (suspected, count) = with(record.tumor) {
            when (bodyLocation) {
                BodyLocationCategory.BONE -> hasSuspectedBoneLesions to lesionCount(hasBoneLesions, boneLesionsCount)
                BodyLocationCategory.BRAIN -> hasSuspectedBrainLesions to lesionCount(hasBrainLesions, brainLesionsCount)
                BodyLocationCategory.CNS -> hasSuspectedCnsLesions to lesionCount(hasCnsLesions, cnsLesionsCount)
                BodyLocationCategory.LIVER -> hasSuspectedLiverLesions to lesionCount(hasLiverLesions, liverLesionsCount)
                BodyLocationCategory.LUNG -> hasSuspectedLungLesions to lesionCount(hasLungLesions, lungLesionsCount)
                BodyLocationCategory.LYMPH_NODE -> hasSuspectedLymphNodeLesions to lesionCount(hasLymphNodeLesions, lymphNodeLesionsCount)
                else -> evaluateOtherLesions(otherLesions, otherSuspectedLesions)
            }
        }

        val messageEnding = "at least $minimumLesions lesions in $bodyLocation"

        return when {
            count == null || suspected == true -> EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
            count >= minimumLesions -> EvaluationFactory.pass("Has $messageEnding")
            else -> EvaluationFactory.fail("Does not have $messageEnding")
        }
    }

    private fun lesionCount(hasLesions: Boolean?, count: Int?): Int? {
        return when (hasLesions) {
            true -> count
            false -> 0
            null -> null
        }
    }

    private fun evaluateOtherLesions(lesions: List<String>?, suspected: List<String>?): Pair<Boolean?, Int?> {
        val count = lesions?.size ?: 0
        return suspected?.isNotEmpty() to if (count >= minimumLesions) null else 0
    }
}