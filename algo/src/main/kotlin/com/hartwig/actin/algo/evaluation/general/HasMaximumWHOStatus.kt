package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasMaximumWHOStatus(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinicalStatus.who
        return when {
            who == null -> EvaluationFactory.undetermined("WHO status is unknown", "WHO status unknown")

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