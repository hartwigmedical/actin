package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.labReferenceWithLimit
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedIndirectBilirubinULN(private val maxULNFactor: Double, private val minValidDate: LocalDate) :
    LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val interpretation = LabInterpreter.interpret(record.labValues)
        check(labValue.measurement == LabMeasurement.DIRECT_BILIRUBIN) { "Indirect bilirubin must take direct bilirubin as input" }
        val mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        if (!LabEvaluation.isValid(mostRecentTotal, LabMeasurement.TOTAL_BILIRUBIN, minValidDate)) {
            return EvaluationFactory.recoverableUndetermined("Indirect bilirubin undetermined (no recent total bilirubin measurement)")
        }

        val labValueString = labValue(LabMeasurement.INDIRECT_BILIRUBIN, mostRecentTotal!!.value - labValue.value, labValue.unit)
        val refLimit = labValue.refLimitUp?.let { mostRecentTotal.refLimitUp?.minus(it) }
        val referenceString = labReferenceWithLimit(maxULNFactor, "ULN", refLimit, labValue.unit)

        return when (LabEvaluation.evaluateDifferenceVersusMaxULN(mostRecentTotal, labValue, maxULNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail("$labValueString exceeds max of $referenceString")
            }
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined("$labValueString exceeds max of $referenceString but within margin of error")
            }
            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined("${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined")
            }
            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass("$labValueString below max of $referenceString")
            }
        }
    }
}