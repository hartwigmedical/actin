package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import kotlin.math.max

class HasMaximumWHOStatus(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinicalStatus.who
        return when {
            who == null -> EvaluationFactory.undetermined("WHO status unknown")

            who <= maximumWHO -> EvaluationFactory.pass(
                "Adequate WHO status"
            )

            who - maximumWHO == 1 -> EvaluationFactory.recoverableFail("WHO $who exceeds max WHO $maximumWHO")

            else -> EvaluationFactory.fail("WHO status $who above requested $maximumWHO")
        }
    }
}