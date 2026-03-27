package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.clinical.interpretation.isAtMost
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision

class HasMaximumWHOStatus(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho
        val evaluation = who?.let { who.isAtMost(maximumWHO) }
        val patientWho = who?.let { who.asText() }

        return when {
            who == null -> EvaluationFactory.undetermined(
                "Undetermined if WHO status is within requested max WHO $maximumWHO (WHO missing)"
            )

            evaluation == EvaluationResult.PASS -> EvaluationFactory.pass("WHO $patientWho is below requested max WHO $maximumWHO")

            evaluation == EvaluationResult.FAIL && who.precision == WhoStatusPrecision.EXACT && who.status - maximumWHO == 1 -> {
                EvaluationFactory.recoverableFail("WHO $patientWho should be below requested max WHO $maximumWHO")
            }

            evaluation == EvaluationResult.FAIL -> EvaluationFactory.fail("WHO $patientWho is not below requested max WHO $maximumWHO")

            evaluation == EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if patient WHO $patientWho is below requested max WHO $maximumWHO")
            }

            else -> throw IllegalStateException("Illegal state exception: HasMaximumWhoStatus")
        }
    }
}
