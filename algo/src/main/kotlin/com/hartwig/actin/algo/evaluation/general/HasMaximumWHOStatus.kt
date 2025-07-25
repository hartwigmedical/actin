package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMaximumWHOStatus(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho
        return when {
            who == null -> EvaluationFactory.undetermined(
                "Undetermined if WHO status is within requested max WHO $maximumWHO (WHO data missing)"
            )

            who <= maximumWHO -> EvaluationFactory.pass("WHO $who is below WHO $maximumWHO")

            who - maximumWHO == 1 -> EvaluationFactory.recoverableFail("WHO $who exceeds WHO $maximumWHO")

            else -> EvaluationFactory.fail("WHO $who exceeds WHO $maximumWHO")
        }
    }
}