package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPlatinumSensitive(private val referenceDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumProgression = PlatinumProgressionAnalysis.create(record, referenceDate)

        return when {
            platinumProgression.hasProgressionOnLastPlatinumWithinSixMonths() == true -> {
                EvaluationFactory.fail("Is platinum resistant")
            }

            platinumProgression.hasProgressionOrUnknownProgressionOnLastPlatinum() == true -> {
                EvaluationFactory.undetermined("Undetermined if patient is platinum sensitive")
            }

            platinumProgression.lastPlatinumTreatment == null -> {
                EvaluationFactory.undetermined("Undetermined if patient is platinum sensitive (no platinum treatment)")
            }

            else -> EvaluationFactory.pass("Is platinum sensitive")
        }
    }
}