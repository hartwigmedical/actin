package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.labReferenceWithLimit
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedIndirectBilirubinULN(
    private val maxULNFactor: Double, private val minValidDate: LocalDate, private val labels: EvaluationLabels.Laboratory
) : SingleLabValueEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val interpretation = LabInterpretation.interpret(record.labValues)
        check(labValue.measurement == LabMeasurement.DIRECT_BILIRUBIN) { "Indirect bilirubin must take direct bilirubin as input" }
        val mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        if (!LabEvaluation.isValid(mostRecentTotal, LabMeasurement.TOTAL_BILIRUBIN, minValidDate)) {
            return EvaluationFactory.recoverableUndetermined(labels.hasLimitedIndirectBilirubinUlnRecoverableUndeterminedNoTotalBilirubin())
        }

        val labValueString = labValue(LabMeasurement.INDIRECT_BILIRUBIN, mostRecentTotal!!.value - labValue.value, labValue.unit)
        val refLimit = labValue.refLimitUp?.let { mostRecentTotal.refLimitUp?.minus(it) }
        val referenceString = labReferenceWithLimit(maxULNFactor, "ULN", refLimit, labValue.unit)

        return when (LabEvaluation.evaluateDifferenceVersusMaxULN(mostRecentTotal, labValue, maxULNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail(labels.hasLimitedIndirectBilirubinUlnRecoverableFail(labValueString, referenceString))
            }
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasLimitedIndirectBilirubinUlnRecoverableUndeterminedWithinMargin(labValueString, referenceString)
                )
            }
            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasLimitedIndirectBilirubinUlnRecoverableUndeterminedCannotDetermine(
                        labMeasurement.display().replaceFirstChar { it.uppercase() }
                    )
                )
            }
            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(labels.hasLimitedIndirectBilirubinUlnPass(labValueString, referenceString))
            }
        }
    }
}