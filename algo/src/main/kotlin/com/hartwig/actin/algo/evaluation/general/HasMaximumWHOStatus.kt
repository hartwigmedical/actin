package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.clinical.interpretation.isAtMost
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision

class HasMaximumWHOStatus(private val maximumWHO: Int, private val labels: EvaluationLabels.General) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho
        val evaluation = who?.let { who.isAtMost(maximumWHO) }
        val patientWho = who?.let { who.asText() }

        return when {
            who == null -> EvaluationFactory.undetermined(labels.hasMaximumWhoStatusUndeterminedMissing(maximumWHO))

            evaluation == EvaluationResult.PASS -> EvaluationFactory.pass(labels.hasMaximumWhoStatusPass(patientWho!!, maximumWHO))

            evaluation == EvaluationResult.FAIL && who.precision == WhoStatusPrecision.EXACT && who.status - maximumWHO == 1 -> {
                EvaluationFactory.recoverableFail(labels.hasMaximumWhoStatusRecoverableFail(patientWho!!, maximumWHO))
            }

            evaluation == EvaluationResult.FAIL -> EvaluationFactory.fail(labels.hasMaximumWhoStatusFail(patientWho!!, maximumWHO))

            evaluation == EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined(labels.hasMaximumWhoStatusUndetermined(patientWho!!, maximumWHO))
            }

            else -> throw IllegalStateException("Illegal state exception: HasMaximumWhoStatus")
        }
    }
}
