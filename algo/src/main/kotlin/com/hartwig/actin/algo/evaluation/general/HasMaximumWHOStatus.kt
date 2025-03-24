package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMaximumWHOStatus(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinicalStatus.who
        return when {
            who == null -> EvaluationFactory.undetermined(
                "Undetermined if WHO status is within requested max WHO $maximumWHO (WHO data missing)"
            )

            who <= maximumWHO -> EvaluationFactory.pass("WHO $who is within requested max WHO $maximumWHO")

            who - maximumWHO == 1 -> EvaluationFactory.recoverableFail("WHO $who exceeds max WHO $maximumWHO")

            else -> EvaluationFactory.fail("WHO $who exceeds max WHO $maximumWHO")
        }
    }
}