package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPrimaryPlatinumRefractoryWithinMonths(
    private val minMonths: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumProgression = PlatinumProgressionAnalysis.create(record, referenceDate)

        return when {
            platinumProgression.hasProgressionOnFirstPlatinumWithinMonths(minMonths) == true -> {
                EvaluationFactory.pass("Is primary platinum refractory")
            }

            platinumProgression.hasProgressionOrUnknownProgressionOnFirstPlatinum() == true -> {
                EvaluationFactory.undetermined("Undetermined if patient is primary platinum refractory")
            }

            platinumProgression.firstPlatinumTreatment == null -> {
                EvaluationFactory.undetermined("Undetermined if patient is primary platinum refractory (no platinum treatment)")
            }

            else -> EvaluationFactory.fail("Not primary platinum refractory (no progression on platinum treatment)")
        }
    }
}