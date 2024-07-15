package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateInvalidLabValue
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateVersusMaxULN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.isValid
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.clinical.interpretation.LabMeasurement.ALANINE_AMINOTRANSFERASE
import com.hartwig.actin.clinical.interpretation.LabMeasurement.ASPARTATE_AMINOTRANSFERASE
import java.time.LocalDate
import java.util.Locale

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

        val ASATLimitEvaluation = evaluateMeasurement(mostRecentASAT, hasLiverMetastases)
        val ALATLimitEvaluation = evaluateMeasurement(mostRecentALAT, hasLiverMetastases)

        val ALATWithinLiverMetastasisLimit = evaluateVersusMaxULN(mostRecentALAT!!, maxULNWithLiverMetastases) == WITHIN_THRESHOLD
        val ASATWithinLiverMetastasisLimit = evaluateVersusMaxULN(mostRecentASAT!!, maxULNWithLiverMetastases) == WITHIN_THRESHOLD

        val ASATLabValueString = createLabValueString(ASPARTATE_AMINOTRANSFERASE, mostRecentASAT)
        val ALATLabValueString = createLabValueString(ALANINE_AMINOTRANSFERASE, mostRecentALAT)
        val ASATReferenceString = createReferenceString(mostRecentASAT, hasLiverMetastases)
        val ALATReferenceString = createReferenceString(mostRecentALAT, hasLiverMetastases)

        return when {
            !checkValidity(mostRecentASAT, ASPARTATE_AMINOTRANSFERASE) -> {
                evaluateInvalidLabValue(ASPARTATE_AMINOTRANSFERASE, mostRecentASAT, minValidLabDate)
            }

            !checkValidity(mostRecentALAT, ALANINE_AMINOTRANSFERASE) -> {
                evaluateInvalidLabValue(ALANINE_AMINOTRANSFERASE, mostRecentALAT, minValidLabDate)
            }

            ASATLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN && ALATLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                val message = "ASAT ($ASATLabValueString) and ALAT ($ALATLabValueString) exceed maximum allowed value"
                evaluateOutsideMargin(ASATWithinLiverMetastasisLimit && ALATWithinLiverMetastasisLimit, hasLiverMetastases, message)
            }

            ASATLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                val message = "$ASATLabValueString exceeds maximum of $ASATReferenceString"
                evaluateOutsideMargin(ASATWithinLiverMetastasisLimit, hasLiverMetastases, message)
            }

            ALATLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                val message = "$ALATLabValueString exceeds maximum of $ALATReferenceString"
                evaluateOutsideMargin(ALATWithinLiverMetastasisLimit, hasLiverMetastases, message)
            }

            ASATLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN && ALATLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message = "ASAT ($ASATLabValueString) and ALAT ($ALATLabValueString) exceed max fold of ULN but within margin of error"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            ASATLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message = "$ASATLabValueString exceeds maximum of $ASATReferenceString but within margin of error"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            ALATLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message = "$ALATLabValueString exceeds maximum of $ALATReferenceString but within margin of error"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            ASATLimitEvaluation == WITHIN_THRESHOLD && ALATLimitEvaluation == WITHIN_THRESHOLD -> {
                val message = "$ASATLabValueString and $ALATLabValueString below max of $ASATReferenceString and $ALATReferenceString"
                EvaluationFactory.recoverablePass(message, message)
            }

            ASATLimitEvaluation == CANNOT_BE_DETERMINED && ALATLimitEvaluation == CANNOT_BE_DETERMINED -> {
                val message = "${createMeasurementString(ASPARTATE_AMINOTRANSFERASE)} " +
                        "and ${createMeasurementString(ALANINE_AMINOTRANSFERASE)} undetermined"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            ASATLimitEvaluation == CANNOT_BE_DETERMINED -> {
                val message = "${createMeasurementString(ASPARTATE_AMINOTRANSFERASE)} undetermined"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            ALATLimitEvaluation == CANNOT_BE_DETERMINED -> {
                val message = "${createMeasurementString(ALANINE_AMINOTRANSFERASE)} undetermined"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            else -> {
                val message = "Unable to determine if ASAT and ALAT within requested fold of ULN."
                EvaluationFactory.undetermined(message, message)
            }
        }
    }

    private fun checkValidity(
        mostRecent: LabValue?, measurement: LabMeasurement
    ): Boolean {
        return isValid(mostRecent, measurement, minValidLabDate) && mostRecent?.date?.isAfter(minPassLabDate) == true
    }

    private fun evaluateMeasurement(mostRecent: LabValue?, hasLiverMetastases: Boolean?): LabEvaluation.LabEvaluationResult {
        return if (hasLiverMetastases == true) {
            evaluateVersusMaxULN(mostRecent!!, maxULNWithLiverMetastases)
        } else {
            evaluateVersusMaxULN(mostRecent!!, maxULNWithoutLiverMetastases)
        }
    }

    private fun createMeasurementString(measurement: LabMeasurement): String {
        return measurement.display().replaceFirstChar { it.uppercase() }
    }

    private fun createReferenceString(mostRecent: LabValue?, hasLiverMetastases: Boolean?): String {
        val max = if (hasLiverMetastases == true) maxULNWithLiverMetastases else maxULNWithoutLiverMetastases
        return "$max*ULN ($max*${mostRecent?.refLimitUp})"
    }

    private fun createLabValueString(measurement: LabMeasurement, mostRecent: LabValue?): String {
        return "${createMeasurementString(measurement)} ${String.format(Locale.ENGLISH, "%.1f", mostRecent?.value)}"
    }

    private fun evaluateOutsideMargin(measurementsWithinLimit: Boolean, hasLiverMetastases: Boolean?, message: String): Evaluation {
        return if (measurementsWithinLimit && hasLiverMetastases == null) {
            val messageEnding = " if no liver metastases present (liver lesion data missing)"
            EvaluationFactory.undetermined(message + messageEnding, message + messageEnding)
        } else {
            EvaluationFactory.recoverableFail(message, message)
        }
    }
}