package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.interpretation.LabInterpretation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedBilirubinPercentageOfTotal(private val maxPercentage: Double, private val minValidDate: LocalDate) :
    LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val interpretation = LabInterpretation.interpret(record.labValues)
        check(labValue.measurement == LabMeasurement.DIRECT_BILIRUBIN) { "Bilirubin percentage must take direct bilirubin as input" }
        val mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        if (mostRecentTotal == null || mostRecentTotal.date.isBefore(minValidDate)) {
            return EvaluationFactory.recoverableUndetermined(
                "Bilirubin percentage of total bilirubin undetermined (no recent total bilirubin measurement)"
            )
        }
        val messageStart = labMeasurement.display().replaceFirstChar { it.uppercase() } + " as percentage of " + mostRecentTotal.measurement.display
        return if ((100 * (labValue.value / mostRecentTotal.value)).compareTo(maxPercentage) <= 0) {
            EvaluationFactory.recoverablePass("$messageStart below max of $maxPercentage%")
        } else {
            EvaluationFactory.recoverableFail("$messageStart exceeds max of $maxPercentage%")
        }
    }
}