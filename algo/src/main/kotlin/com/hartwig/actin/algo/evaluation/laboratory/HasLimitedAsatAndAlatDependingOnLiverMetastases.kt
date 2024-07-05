package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateVersusMaxULN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.isValid
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.clinical.interpretation.LabMeasurement.ASPARTATE_AMINOTRANSFERASE
import com.hartwig.actin.clinical.interpretation.LabMeasurement.ALANINE_AMINOTRANSFERASE
import java.time.LocalDate

class HasLimitedAsatAndAlatDependingOnLiverMetastases(
    private val maxULNWithoutLiverMetastases: Double,
    private val maxULNWithLiverMetastases: Double,
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.tumor.hasLiverLesions
        val interpreter = LabInterpreter.interpret(record.labValues)
        val mostRecentASAT = interpreter.mostRecentValue(ASPARTATE_AMINOTRANSFERASE)
        val mostRecentALAT = interpreter.mostRecentValue(ALANINE_AMINOTRANSFERASE)

        if (!isValid(mostRecentASAT, ALANINE_AMINOTRANSFERASE, minValidLabDate) || mostRecentASAT?.date?.isAfter(minPassLabDate) == false) {
            return LabEvaluation.evaluateInvalidLabValue(ASPARTATE_AMINOTRANSFERASE, mostRecentASAT, minValidLabDate)
        }
        if (!isValid(mostRecentALAT, ALANINE_AMINOTRANSFERASE, minValidLabDate) || mostRecentALAT?.date?.isAfter(minPassLabDate) == false) {
            return LabEvaluation.evaluateInvalidLabValue(ALANINE_AMINOTRANSFERASE, mostRecentALAT, minValidLabDate)
        }

        val ASATLimitEvaluation = evaluateMeasurement(mostRecentASAT, hasLiverMetastases)
        val ALATLimitEvaluation = evaluateMeasurement(mostRecentALAT, hasLiverMetastases)

        val ALATWithinLiverMetastasisLimit = evaluateVersusMaxULN(mostRecentALAT!!, maxULNWithLiverMetastases) == WITHIN_THRESHOLD
        val ASATWithinLiverMetastasisLimit = evaluateVersusMaxULN(mostRecentASAT!!, maxULNWithLiverMetastases) == WITHIN_THRESHOLD

        val ASATLabValueString = createLabValueString(ASPARTATE_AMINOTRANSFERASE, mostRecentASAT)
        val ALATLabValueString = createLabValueString(ALANINE_AMINOTRANSFERASE, mostRecentALAT)
        val ASATReferenceString = createReferenceString(mostRecentASAT, hasLiverMetastases)
        val ALATReferenceString = createReferenceString(mostRecentALAT, hasLiverMetastases)

        return when {
            ASATLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                if (ASATWithinLiverMetastasisLimit && hasLiverMetastases == null) {
                    val message = "$ASATLabValueString exceeds maximum of $ASATReferenceString if no liver metastases present " +
                            "(liver lesion data missing)"
                    EvaluationFactory.undetermined(message, message)
                } else {
                    val message = "$ASATLabValueString exceeds maximum of $ASATReferenceString"
                    EvaluationFactory.recoverableFail(message, message)
                }
            }

            ALATLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                if (ALATWithinLiverMetastasisLimit && hasLiverMetastases == null) {
                    val message = "$ALATLabValueString exceeds maximum of $ALATReferenceString if no liver metastases present " +
                            "(liver lesion data missing)"
                    EvaluationFactory.undetermined(message, message)
                } else {
                    val message = "$ALATLabValueString exceeds maximum of $ALATReferenceString"
                    EvaluationFactory.recoverableFail(message, message)
                }
            }

            ASATLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message = "$ASATLabValueString exceeds maximum of $ASATReferenceString"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            ALATLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message = "$ALATLabValueString exceeds maximum of $ALATReferenceString"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            (ASATLimitEvaluation == WITHIN_THRESHOLD && ALATLimitEvaluation == WITHIN_THRESHOLD) -> {
                val message = "$ASATLabValueString and $ALATLabValueString below max of $ASATReferenceString and $ALATReferenceString"
                EvaluationFactory.recoverablePass(message, message)
            }

            ASATLimitEvaluation == CANNOT_BE_DETERMINED -> {
                val message = "${ASPARTATE_AMINOTRANSFERASE.display().replaceFirstChar { it.uppercase() }} undetermined"
                EvaluationFactory.undetermined(message, message)
            }

            else -> {
                val message = "${ALANINE_AMINOTRANSFERASE.display().replaceFirstChar { it.uppercase() }} undetermined"
                EvaluationFactory.undetermined(message, message)
            }
        }
    }

    private fun evaluateMeasurement(mostRecent: LabValue?, hasLiverMetastases: Boolean?): LabEvaluation.LabEvaluationResult {
        return if (hasLiverMetastases == true) {
            evaluateVersusMaxULN(mostRecent!!, maxULNWithLiverMetastases)
        } else {
            evaluateVersusMaxULN(mostRecent!!, maxULNWithoutLiverMetastases)
        }
    }

    private fun createReferenceString(mostRecent: LabValue?, hasLiverMetastases: Boolean?): String {
        val max = if (hasLiverMetastases == true) maxULNWithLiverMetastases else maxULNWithoutLiverMetastases
        return "$max*ULN ($max*${mostRecent?.refLimitUp})"
    }

    private fun createLabValueString(measurement: LabMeasurement, mostRecent: LabValue?): String {
        return "${measurement.display().replaceFirstChar { it.uppercase() }} ${String.format("%.1f", mostRecent?.value)}"
    }

}