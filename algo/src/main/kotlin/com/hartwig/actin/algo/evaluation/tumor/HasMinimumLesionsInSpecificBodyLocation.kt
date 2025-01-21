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

        val (hasLesions, hasSuspectedLesions, count) = with(record.tumor) {
            when (bodyLocation) {
                BodyLocationCategory.BONE -> Triple(hasBoneLesions, hasSuspectedBoneLesions, boneLesionsCount)
                BodyLocationCategory.BRAIN -> Triple(hasBrainLesions, hasSuspectedBrainLesions, brainLesionsCount)
                BodyLocationCategory.CNS -> Triple(hasCnsLesions, hasSuspectedCnsLesions, cnsLesionsCount)
                BodyLocationCategory.LIVER -> Triple(hasLiverLesions, hasSuspectedLiverLesions, liverLesionsCount)
                BodyLocationCategory.LUNG -> Triple(hasLungLesions, hasSuspectedLungLesions, lungLesionsCount)
                BodyLocationCategory.LYMPH_NODE -> Triple(hasLymphNodeLesions, hasSuspectedLymphNodeLesions, lymphNodeLesionsCount)
                else -> return EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
            }
        }

        return when {
            (count ?: if (hasLesions == true) 1 else 0) >= minimumLesions -> EvaluationFactory.pass("Has $messageEnding")

            hasSuspectedLesions == true || count == null -> EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")

            else -> EvaluationFactory.fail("Does not have $messageEnding")
        }
    }
}