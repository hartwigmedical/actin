package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPlatinumResistant(private val referenceDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumProgression = PlatinumProgressionAnalysis.create(record, referenceDate, 6)

        return when {
            platinumProgression.hasProgressionDuringPlatinumOrWithinMonths(platinumProgression.lastPlatinumTreatment) == true -> {
                EvaluationFactory.pass("Is platinum resistant")
            }

            platinumProgression.hasProgressionOrUnknownProgressionOnPlatinum(platinumProgression.lastPlatinumTreatment) == true -> {
                EvaluationFactory.undetermined("Undetermined if patient is platinum resistant")
            }

            platinumProgression.lastPlatinumTreatment == null -> {
                EvaluationFactory.undetermined("Undetermined if patient is platinum resistant (no platinum treatment)")
            }

            else -> EvaluationFactory.fail("Is platinum sensitive")
        }
    }
}