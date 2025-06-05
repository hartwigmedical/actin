package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory

class HasMinimumLesionsInSpecificBodyLocation(
    private val minLesions: Int, private val bodyLocation: BodyLocationCategory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val messageEnding = "at least $minLesions lesions in ${bodyLocation.display()}"

        val (hasLesions, hasSuspectedLesions) = with(record.tumor) {
            when (bodyLocation) {
                BodyLocationCategory.BONE -> Pair(hasBoneLesions, hasSuspectedBoneLesions)
                BodyLocationCategory.BRAIN -> Pair(hasBrainLesions, hasSuspectedBrainLesions)
                BodyLocationCategory.CNS -> Pair(hasCnsLesions, hasSuspectedCnsLesions)
                BodyLocationCategory.LIVER -> Pair(hasLiverLesions, hasSuspectedLiverLesions)
                BodyLocationCategory.LUNG -> Pair(hasLungLesions, hasSuspectedLungLesions)
                BodyLocationCategory.LYMPH_NODE -> Pair(hasLymphNodeLesions, hasSuspectedLymphNodeLesions)
                else -> return EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
            }
        }

        return when {
            minLesions <= 1 && hasLesions == true -> {
                EvaluationFactory.pass("Patient has $messageEnding")
            }

            hasLesions != false || hasSuspectedLesions == true -> {
                EvaluationFactory.undetermined("Undetermined if patient has $messageEnding")
            }

            else -> EvaluationFactory.fail("Does not have $messageEnding")
        }
    }
}