package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import kotlin.math.abs

class HasWHOStatus internal constructor(private val requiredWHO: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinicalStatus.who
        val warningComplicationCategories = WHOFunctions.findComplicationCategoriesAffectingWHOStatus(record)
        return when {
            who == null -> {
                EvaluationFactory.recoverableUndetermined("WHO status is unknown", "WHO status unknown")
            }

            who == requiredWHO && warningComplicationCategories.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Patient WHO status $who matches requested but patient has complication categories of concern: "
                            + concatLowercaseWithAnd(warningComplicationCategories) + ", potentially indicating deterioration",
                    "WHO currently adequate but potential deterioration due to" + concatLowercaseWithAnd(
                        warningComplicationCategories
                    )
                )
            }

            who == requiredWHO -> {
                EvaluationFactory.pass(
                    "Patient WHO status $who is requested WHO (WHO $requiredWHO)",
                    "Adequate WHO status"
                )
            }

            abs(who - requiredWHO) == 1 -> {
                EvaluationFactory.recoverableFail(
                    "Patient WHO status $who is close to requested WHO (WHO $requiredWHO)",
                    "WHO status is $who but should be $requiredWHO"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient WHO status $who is not requested WHO (WHO $requiredWHO)",
                    "WHO status is $who but should be $requiredWHO"
                )
            }
        }
    }
}