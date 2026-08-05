package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPlatinumResistant(private val referenceDate: LocalDate, private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumProgression = PlatinumProgressionAnalysis.create(record, referenceDate, 6)

        return when {
            platinumProgression.hasProgressionDuringPlatinumOrWithinMonths(platinumProgression.lastPlatinumTreatment) == true -> {
                EvaluationFactory.pass(labels.isPlatinumResistantPass())
            }

            platinumProgression.hasProgressionOrUnknownProgressionOnPlatinum(platinumProgression.lastPlatinumTreatment) == true -> {
                EvaluationFactory.undetermined(labels.isPlatinumResistantUndetermined())
            }

            platinumProgression.lastPlatinumTreatment == null -> {
                EvaluationFactory.undetermined(labels.isPlatinumResistantUndeterminedNoPlatinumTreatment())
            }

            else -> EvaluationFactory.fail(labels.isPlatinumResistantFail())
        }
    }
}