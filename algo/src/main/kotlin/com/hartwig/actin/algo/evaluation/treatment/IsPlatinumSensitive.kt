package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPlatinumSensitive(private val referenceDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumProgression = PlatinumProgressionFunctions.create(record)

        return when {
            platinumProgression.hasProgressionOnPlatinumWithinSixMonths(referenceDate) -> {
                EvaluationFactory.fail("Is platinum resistant")
            }

            platinumProgression.hasProgressionOrUnknownProgressionOnPlatinum() -> {
                EvaluationFactory.undetermined("Undetermined if patient is platinum sensitive")
            }

            platinumProgression.platinumTreatment == null -> {
                EvaluationFactory.undetermined("Undetermined if patient is platinum sensitive (no platinum treatment)")
            }

            else -> EvaluationFactory.pass("Is platinum sensitive")
        }
    }
}