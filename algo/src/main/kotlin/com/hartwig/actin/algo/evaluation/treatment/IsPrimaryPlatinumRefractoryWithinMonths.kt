package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPrimaryPlatinumRefractoryWithinMonths(private val minMonths: Int, private val referenceDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumProgression = PlatinumProgressionAnalysis.create(record, referenceDate, minMonths)

        return when {
            platinumProgression.hasProgressionDuringPlatinumOrWithinMonths(platinumProgression.firstPlatinumTreatment) == true -> {
                EvaluationFactory.pass("Primary platinum refractory disease")
            }

            platinumProgression.hasProgressionOrUnknownProgressionOnPlatinum(platinumProgression.firstPlatinumTreatment) == true -> {
                EvaluationFactory.undetermined("Undetermined if primary platinum refractory disease")
            }

            platinumProgression.firstPlatinumTreatment == null -> {
                EvaluationFactory.undetermined("Undetermined if primary platinum refractory disease (no platinum treatment)")
            }

            else -> EvaluationFactory.fail("No primary platinum refractory disease (no progression on platinum treatment)")
        }
    }
}