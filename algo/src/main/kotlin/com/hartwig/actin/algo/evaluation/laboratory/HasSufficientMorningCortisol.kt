package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientMorningCortisol(private val minLLNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {

        val cortisolValue = "Cortisol ${String.format("%.1f", labValue.value)}"
        val referenceCortisol = "$minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow})"

        return when (LabEvaluation.evaluateVersusMinLLN(labValue, minLLNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN, LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    "$cortisolValue is below minimum of $referenceCortisol, undetermined if value is a morning measurement", "$cortisolValue below min of $referenceCortisol, undetermined if value is a morning measurement"
                )
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} could not be evaluated against minimum LLN",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined"
                )
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(
                    "$cortisolValue above minimum of $referenceCortisol", "$cortisolValue above min of $referenceCortisol"
                )
            }
        }
    }
}