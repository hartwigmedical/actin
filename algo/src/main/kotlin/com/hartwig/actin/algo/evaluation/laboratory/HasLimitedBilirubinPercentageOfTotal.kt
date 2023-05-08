package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

class HasLimitedBilirubinPercentageOfTotal internal constructor(private val maxPercentage: Double, private val minValidDate: LocalDate) :
    LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val interpretation = LabInterpreter.interpret(record.clinical().labValues())
        check(labValue.code() == LabMeasurement.DIRECT_BILIRUBIN.code()) { "Bilirubin percentage must take direct bilirubin as input" }
        val mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        if (mostRecentTotal == null || mostRecentTotal.date().isBefore(minValidDate)) {
            return EvaluationFactory.undetermined(
                "No recent measurement found for total bilirubin, hence bilirubin percentage of total bilirubin could not be determined",
                "Bilirubin percentage of total bilirubin could not be determined"
            )
        }
        val messageStart = labValue.code() + " as percentage of " + mostRecentTotal.code()
        return if ((100 * (labValue.value() / mostRecentTotal.value())).compareTo(maxPercentage) <= 0) {
            EvaluationFactory.recoverablePass(
                "$messageStart is below maximum percentage of $maxPercentage%",
                "$messageStart below $maxPercentage%"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "$messageStart exceeds maximum percentage of $maxPercentage%",
                "$messageStart exceeding $maxPercentage%"
            )
        }
    }
}