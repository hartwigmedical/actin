package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory

class HasMinimumLesionsInSpecificBodyLocation(
    private val minLesions: Int, private val bodyLocation: BodyLocationCategory, private val labels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val bodyLocationDisplay = bodyLocation.display()

        val (hasLesions, hasSuspectedLesions) = with(record.tumor) {
            when (bodyLocation) {
                BodyLocationCategory.BONE -> Pair(hasBoneLesions, hasSuspectedBoneLesions)
                BodyLocationCategory.BRAIN -> Pair(hasBrainLesions, hasSuspectedBrainLesions)
                BodyLocationCategory.CNS -> Pair(hasCnsLesions, hasSuspectedCnsLesions)
                BodyLocationCategory.LIVER -> Pair(hasLiverLesions, hasSuspectedLiverLesions)
                BodyLocationCategory.LUNG -> Pair(hasLungLesions, hasSuspectedLungLesions)
                BodyLocationCategory.LYMPH_NODE -> Pair(hasLymphNodeLesions, hasSuspectedLymphNodeLesions)
                else -> return EvaluationFactory.undetermined(
                    labels.hasMinimumLesionsInSpecificBodyLocationUndetermined(minLesions, bodyLocationDisplay)
                )
            }
        }

        return when {
            minLesions <= 1 && hasLesions == true -> {
                EvaluationFactory.pass(labels.hasMinimumLesionsInSpecificBodyLocationPass(minLesions, bodyLocationDisplay))
            }

            hasLesions != false || hasSuspectedLesions == true -> {
                EvaluationFactory.undetermined(
                    labels.hasMinimumLesionsInSpecificBodyLocationUndetermined(minLesions, bodyLocationDisplay)
                )
            }

            else -> EvaluationFactory.fail(labels.hasMinimumLesionsInSpecificBodyLocationFail(minLesions, bodyLocationDisplay))
        }
    }
}