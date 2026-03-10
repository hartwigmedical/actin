package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.asText
import com.hartwig.actin.clinical.interpretation.isEqualTo
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import kotlin.math.abs

class HasWHOStatus(private val requiredWHO: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho
        val evaluation = who?.let { who.isEqualTo(requiredWHO) }
        val patientWho = who?.let { who.asText() }

        return when {
            who == null -> {
                EvaluationFactory.undetermined("Undetermined if WHO status is equal to requested WHO $requiredWHO (WHO data missing)")
            }

            evaluation == EvaluationResult.PASS -> EvaluationFactory.pass("WHO $patientWho is exactly requested WHO $requiredWHO")

            evaluation == EvaluationResult.FAIL && who.precision == WhoStatusPrecision.EXACT && abs(who.status - requiredWHO) == 1 -> {
                EvaluationFactory.recoverableFail("Patient WHO is $patientWho but should be exactly WHO $requiredWHO")
            }

            evaluation == EvaluationResult.FAIL -> {
                EvaluationFactory.fail("Patient WHO is $patientWho but should be exactly WHO $requiredWHO")
            }

            evaluation == EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if patient WHO $patientWho is exactly WHO $requiredWHO")
            }

            else -> throw IllegalStateException("Illegal state exception: HasWhoStatus")
        }
    }
}