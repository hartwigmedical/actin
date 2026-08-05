package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class IsPrimaryPlatinumRefractoryWithinMonths(
    private val minMonths: Int,
    private val referenceDate: LocalDate,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumProgression = PlatinumProgressionAnalysis.create(record, referenceDate, minMonths)

        return when {
            platinumProgression.hasProgressionDuringPlatinumOrWithinMonths(platinumProgression.firstPlatinumTreatment) == true -> {
                EvaluationFactory.pass(labels.isPrimaryPlatinumRefractoryWithinMonthsPass())
            }

            platinumProgression.hasProgressionOrUnknownProgressionOnPlatinum(platinumProgression.firstPlatinumTreatment) == true -> {
                EvaluationFactory.undetermined(labels.isPrimaryPlatinumRefractoryWithinMonthsUndetermined())
            }

            platinumProgression.firstPlatinumTreatment == null -> {
                EvaluationFactory.undetermined(labels.isPrimaryPlatinumRefractoryWithinMonthsUndeterminedNoPlatinumTreatment())
            }

            else -> EvaluationFactory.fail(labels.isPrimaryPlatinumRefractoryWithinMonthsFail())
        }
    }
}