package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedBilirubinPercentageOfTotal(private val maxPercentage: Double, private val minValidDate: LocalDate) :
    LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val interpretation = LabInterpreter.interpret(record.labValues)
        check(labValue.code == LabMeasurement.DIRECT_BILIRUBIN.code) { "Bilirubin percentage must take direct bilirubin as input" }
        val mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        if (mostRecentTotal == null || mostRecentTotal.date.isBefore(minValidDate)) {
            return EvaluationFactory.recoverableUndetermined(
                "No recent measurement found for total bilirubin, hence bilirubin percentage of total bilirubin could not be determined",
                "Bilirubin percentage of total bilirubin could not be determined"
            )
        }
        val messageStart = labMeasurement.display().replaceFirstChar { it.uppercase() } + " as percentage of " + mostRecentTotal.code
        return if ((100 * (labValue.value / mostRecentTotal.value)).compareTo(maxPercentage) <= 0) {
            EvaluationFactory.recoverablePass(
                "$messageStart below maximum percentage of $maxPercentage%",
                "$messageStart below max of $maxPercentage%"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "$messageStart exceeds maximum percentage of $maxPercentage%",
                "$messageStart exceeds max of $maxPercentage%"
            )
        }
    }
}