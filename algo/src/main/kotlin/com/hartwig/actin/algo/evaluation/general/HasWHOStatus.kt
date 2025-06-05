package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import kotlin.math.abs

class HasWHOStatus(private val requiredWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinicalStatus.who
        return when {
            who == null -> {
                EvaluationFactory.undetermined("Undetermined if WHO status is required WHO $requiredWHO (WHO data missing)")
            }

            who == requiredWHO -> {
                EvaluationFactory.pass("Has WHO status $requiredWHO")
            }

            abs(who - requiredWHO) == 1 -> {
                EvaluationFactory.recoverableFail("WHO status is $who but should be $requiredWHO")
            }

            else -> {
                EvaluationFactory.fail("WHO status is $who but should be $requiredWHO")
            }
        }
    }
}