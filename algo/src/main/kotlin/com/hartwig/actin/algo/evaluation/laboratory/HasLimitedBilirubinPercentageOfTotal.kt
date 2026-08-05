package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedBilirubinPercentageOfTotal(
    private val maxPercentage: Double, private val minValidDate: LocalDate, private val labels: EvaluationLabels.Laboratory
) : SingleLabValueEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val interpretation = LabInterpretation.interpret(record.labValues)
        check(labValue.measurement == LabMeasurement.DIRECT_BILIRUBIN) { "Bilirubin percentage must take direct bilirubin as input" }
        val mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        if (mostRecentTotal == null || mostRecentTotal.date.isBefore(minValidDate)) {
            return EvaluationFactory.recoverableUndetermined(labels.hasLimitedBilirubinPercentageOfTotalRecoverableUndetermined())
        }
        val messageStart = labels.hasLimitedBilirubinPercentageOfTotalMessageStart(
            labMeasurement.display().replaceFirstChar { it.uppercase() }, mostRecentTotal.measurement.display
        )
        return if ((100 * (labValue.value / mostRecentTotal.value)).compareTo(maxPercentage) <= 0) {
            EvaluationFactory.recoverablePass(labels.hasLimitedBilirubinPercentageOfTotalPass(messageStart, maxPercentage))
        } else {
            EvaluationFactory.recoverableFail(labels.hasLimitedBilirubinPercentageOfTotalRecoverableFail(messageStart, maxPercentage))
        }
    }
}