package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format

class HasMaximumWHOStatus internal constructor(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinicalStatus.who
        val warningComplicationCategories: Set<String> = WHOFunctions.findComplicationCategoriesAffectingWHOStatus(record)
        return when {
            who == null -> EvaluationFactory.undetermined("WHO status is unknown", "WHO status unknown")
            who == maximumWHO && warningComplicationCategories.isNotEmpty() -> EvaluationFactory.warn(
                "Patient WHO status $who equals maximum but patient has complication categories of concern: " + Format.concatLowercaseWithAnd(
                    warningComplicationCategories
                ) + ", potentially indicating deterioration",
                "WHO currently adequate but potential deterioration due to " + Format.concatLowercaseWithAnd(
                    warningComplicationCategories
                )
            )

            who <= maximumWHO -> EvaluationFactory.pass(
                "Patient WHO status $who is within requested max (WHO $maximumWHO)", "Adequate WHO status"
            )

            who - maximumWHO == 1 -> EvaluationFactory.recoverableFail(
                "Patient WHO status $who is 1 higher than requested max (WHO $maximumWHO)",
                "WHO $who exceeds max WHO $maximumWHO"
            )

            else -> EvaluationFactory.fail(
                "Patient WHO status $who is worse than requested max (WHO $maximumWHO)", "WHO status $who too high"
            )
        }
    }
}