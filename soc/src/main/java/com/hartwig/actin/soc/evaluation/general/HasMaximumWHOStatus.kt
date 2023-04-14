package com.hartwig.actin.soc.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.soc.evaluation.EvaluationFactory
import com.hartwig.actin.soc.evaluation.EvaluationFunction
import com.hartwig.actin.soc.evaluation.util.Format

class HasMaximumWHOStatus internal constructor(private val maximumWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinical().clinicalStatus().who()
                ?: return EvaluationFactory.recoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages("WHO status is missing")
                        .addUndeterminedGeneralMessages("WHO status missing")
                        .build()
        val warningComplicationCategories: Set<String> = WHOFunctions.findComplicationCategoriesAffectingWHOStatus(record)
        return if (who == maximumWHO && !warningComplicationCategories.isEmpty()) {
            EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(
                            "Patient WHO status " + who + " equals maximum but patient has complication categories of concern: "
                                    + Format.concat(warningComplicationCategories))
                    .addWarnGeneralMessages("WHO adequate but has " + Format.concat(warningComplicationCategories))
                    .build()
        } else if (who <= maximumWHO) {
            EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient WHO status $who is within requested max (WHO $maximumWHO)")
                    .addPassGeneralMessages("Adequate WHO status")
                    .build()
        } else if (who - maximumWHO == 1) {
            EvaluationFactory.recoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient WHO status $who is 1 higher than requested max (WHO $maximumWHO)")
                    .addFailGeneralMessages("WHO $who, max allowed WHO is $maximumWHO")
                    .build()
        } else {
            EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient WHO status $who is worse than requested max (WHO $maximumWHO)")
                    .addFailGeneralMessages("WHO status $who too high")
                    .build()
        }
    }
}