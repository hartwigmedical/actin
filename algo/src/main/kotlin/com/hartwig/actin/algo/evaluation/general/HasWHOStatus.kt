package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.clinical.interpretation.isExactly
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import kotlin.math.abs

class HasWHOStatus(private val requiredWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho
        return when {
            who == null -> {
                EvaluationFactory.undetermined("Undetermined if WHO status is required WHO $requiredWHO (WHO data missing)")
            }

            who.precision == WhoStatusPrecision.AT_LEAST -> {
                EvaluationFactory.undetermined("Undetermined if WHO status is required WHO $requiredWHO (exact WHO not available)")
            }

            who.isExactly(requiredWHO) -> {
                EvaluationFactory.pass("Has WHO status $requiredWHO")
            }

            abs(who.status - requiredWHO) == 1 -> {
                EvaluationFactory.recoverableFail("WHO status is ${who.asText()} but should be $requiredWHO")
            }

            else -> {
                EvaluationFactory.fail("WHO status is ${who.asText()} but should be $requiredWHO")
            }
        }
    }
}


