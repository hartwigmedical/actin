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
        val messageEnding = "at least $minimumLesions lesions in ${bodyLocation.display()}"

        val (hasLesions, hasSuspectedLesions, minCount) = with(record.tumor) {
            when (bodyLocation) {
                BodyLocationCategory.BONE -> Triple(hasBoneLesions, hasSuspectedBoneLesions, boneLesionsMinCount)
                BodyLocationCategory.BRAIN -> Triple(hasBrainLesions, hasSuspectedBrainLesions, brainLesionsMinCount)
                BodyLocationCategory.CNS -> Triple(hasCnsLesions, hasSuspectedCnsLesions, cnsLesionsMinCount)
                BodyLocationCategory.LIVER -> Triple(hasLiverLesions, hasSuspectedLiverLesions, liverLesionsMinCount)
                BodyLocationCategory.LUNG -> Triple(hasLungLesions, hasSuspectedLungLesions, lungLesionsMinCount)
                BodyLocationCategory.LYMPH_NODE -> Triple(hasLymphNodeLesions, hasSuspectedLymphNodeLesions, lymphNodeLesionsMinCount)
                else -> return EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
            }
        }

        return when {
            (minCount ?: 0) >= minimumLesions -> EvaluationFactory.pass("Has $messageEnding")

            hasLesions != false || hasSuspectedLesions == true -> {
                EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
            }

            else -> EvaluationFactory.fail("Does not have $messageEnding")
        }
    }
}