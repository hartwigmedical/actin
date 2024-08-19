package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

class HasLimitedIndirectBilirubinULN(private val maxULNFactor: Double, private val minValidDate: LocalDate) :
    LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val interpretation = LabInterpreter.interpret(record.labValues)
        check(labValue.code == LabMeasurement.DIRECT_BILIRUBIN.code) { "Indirect bilirubin must take direct bilirubin as input" }
        val mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        if (!LabEvaluation.isValid(mostRecentTotal, LabMeasurement.TOTAL_BILIRUBIN, minValidDate)) {
            return EvaluationFactory.recoverableUndetermined(
                "No recent measurement found for total bilirubin, hence indirect bilirubin could not be determined",
                "Indirect bilirubin could not be determined"
            )
        }

        val labValueString = "Indirect bilirubin ${String.format("%.1f", mostRecentTotal!!.value - labValue.value)}"
        val referenceString = "$maxULNFactor*ULN ($maxULNFactor*${String.format("%.1f", labValue.refLimitUp?.let { mostRecentTotal.refLimitUp?.minus(it) })})"

        return when (LabEvaluation.evaluateDifferenceVersusMaxULN(mostRecentTotal, labValue, maxULNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString exceeds maximum of $referenceString", "$labValueString exceeds max of $referenceString"
                )
            }
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    "$labValueString exceeds maximum of $referenceString", "$labValueString exceeds max of $referenceString"
                )
            }
            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} could not be evaluated against maximum ULN",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined"
                )
            }
            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString below maximum of $referenceString", "$labValueString below max of $referenceString"
                )
            }
        }
    }
}