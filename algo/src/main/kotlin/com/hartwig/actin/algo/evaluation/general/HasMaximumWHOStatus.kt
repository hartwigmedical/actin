package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision

class HasMaximumWHOStatus(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho
        return when {
            who == null -> EvaluationFactory.undetermined(
                "Undetermined if WHO status is within requested max WHO $maximumWHO (WHO data missing)"
            )

            who.precision == WhoStatusPrecision.AT_LEAST -> EvaluationFactory.undetermined(
                "Undetermined if WHO status is within requested max WHO $maximumWHO (Only minimum WHO available)"
            )

            who.status <= maximumWHO -> EvaluationFactory.pass(
                "WHO ${who.asText()} is below WHO $maximumWHO"
            )

            who.status - maximumWHO == 1 -> EvaluationFactory.recoverableFail(
                "WHO ${who.asText()} exceeds WHO $maximumWHO"
            )

            else -> EvaluationFactory.fail("WHO ${who.asText()} exceeds WHO $maximumWHO")
        }
    }
}
