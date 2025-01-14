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
        val messageEnding = "at least $minimumLesions lesions in $bodyLocation"

        val (suspected, count) = with(record.tumor) {
            when (bodyLocation) {
                BodyLocationCategory.BONE -> hasSuspectedBoneLesions to lesionCount(hasBoneLesions, boneLesionsCount)
                BodyLocationCategory.BRAIN -> hasSuspectedBrainLesions to lesionCount(hasBrainLesions, brainLesionsCount)
                BodyLocationCategory.CNS -> hasSuspectedCnsLesions to lesionCount(hasCnsLesions, cnsLesionsCount)
                BodyLocationCategory.LIVER -> hasSuspectedLiverLesions to lesionCount(hasLiverLesions, liverLesionsCount)
                BodyLocationCategory.LUNG -> hasSuspectedLungLesions to lesionCount(hasLungLesions, lungLesionsCount)
                BodyLocationCategory.LYMPH_NODE -> hasSuspectedLymphNodeLesions to lesionCount(hasLymphNodeLesions, lymphNodeLesionsCount)
                else -> return EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
            }
        }

        return when {
            (count ?: 0) >= minimumLesions -> EvaluationFactory.pass("Has $messageEnding")
            count == null || suspected == true -> EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
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
}