package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateInvalidLabValue
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateVersusMaxULN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.isValid
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabMeasurement.ALANINE_AMINOTRANSFERASE
import com.hartwig.actin.datamodel.clinical.LabMeasurement.ASPARTATE_AMINOTRANSFERASE
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.util.ApplicationConfig
import java.time.LocalDate

class HasLimitedAsatAndAlatDependingOnLiverMetastases(
    private val maxULNWithoutLiverMetastases: Double,
    private val maxULNWithLiverMetastases: Double,
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate,
    private val labels: EvaluationLabels.Laboratory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.tumor.hasLiverLesions
        val interpreter = LabInterpretation.interpret(record.labValues)
        val mostRecentAsat = interpreter.mostRecentValue(ASPARTATE_AMINOTRANSFERASE)
        val mostRecentAlat = interpreter.mostRecentValue(ALANINE_AMINOTRANSFERASE)

        val asatLimitEvaluation = evaluateMeasurement(mostRecentAsat, hasLiverMetastases)
        val alatLimitEvaluation = evaluateMeasurement(mostRecentAlat, hasLiverMetastases)

        val alatWithinLiverMetastasisLimit = mostRecentAlat?.let { evaluateVersusMaxULN(it, maxULNWithLiverMetastases) == WITHIN_THRESHOLD }
        val asatWithinLiverMetastasisLimit = mostRecentAsat?.let { evaluateVersusMaxULN(it, maxULNWithLiverMetastases) == WITHIN_THRESHOLD }

        val asatLabValueString = createLabValueString(ASPARTATE_AMINOTRANSFERASE, mostRecentAsat)
        val alatLabValueString = createLabValueString(ALANINE_AMINOTRANSFERASE, mostRecentAlat)
        val asatReferenceString = createReferenceString(mostRecentAsat, hasLiverMetastases)
        val alatReferenceString = createReferenceString(mostRecentAlat, hasLiverMetastases)

        return when {
            !checkValidity(mostRecentAsat, ASPARTATE_AMINOTRANSFERASE) && !checkValidity(mostRecentAlat, ALANINE_AMINOTRANSFERASE) -> {
                EvaluationFactory.recoverableUndetermined(labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedNoData())
            }

            asatLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN && alatLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                val message = labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesExceedMaxBoth(asatLabValueString, alatLabValueString)
                evaluateOutsideMargin(
                    asatWithinLiverMetastasisLimit == true && alatWithinLiverMetastasisLimit == true, hasLiverMetastases, message
                )
            }

            asatLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                val message = labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesExceedMax(asatLabValueString, asatReferenceString)
                evaluateOutsideMargin(asatWithinLiverMetastasisLimit == true, hasLiverMetastases, message)
            }

            alatLimitEvaluation == EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                val message = labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesExceedMax(alatLabValueString, alatReferenceString)
                evaluateOutsideMargin(alatWithinLiverMetastasisLimit == true, hasLiverMetastases, message)
            }

            !checkValidity(mostRecentAsat, ASPARTATE_AMINOTRANSFERASE) -> {
                evaluateInvalidLabValue(ASPARTATE_AMINOTRANSFERASE, mostRecentAsat, minValidLabDate, labels)
            }

            !checkValidity(mostRecentAlat, ALANINE_AMINOTRANSFERASE) -> {
                evaluateInvalidLabValue(ALANINE_AMINOTRANSFERASE, mostRecentAlat, minValidLabDate, labels)
            }

            asatLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN && alatLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message =
                    labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedWithinMarginBoth(
                        asatLabValueString, alatLabValueString
                    )
                EvaluationFactory.recoverableUndetermined(message)
            }

            asatLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message =
                    labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedWithinMargin(
                        asatLabValueString, asatReferenceString
                    )
                EvaluationFactory.recoverableUndetermined(message)
            }

            alatLimitEvaluation == EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                val message =
                    labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedWithinMargin(
                        alatLabValueString, alatReferenceString
                    )
                EvaluationFactory.recoverableUndetermined(message)
            }

            asatLimitEvaluation == WITHIN_THRESHOLD && alatLimitEvaluation == WITHIN_THRESHOLD -> {
                val message = labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesPass(
                    asatLabValueString, alatLabValueString, asatReferenceString, alatReferenceString
                )
                EvaluationFactory.recoverablePass(message)
            }

            asatLimitEvaluation == CANNOT_BE_DETERMINED && alatLimitEvaluation == CANNOT_BE_DETERMINED -> {
                val message = labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedBoth(
                    createMeasurementString(ASPARTATE_AMINOTRANSFERASE), createMeasurementString(ALANINE_AMINOTRANSFERASE)
                )
                EvaluationFactory.recoverableUndetermined(message)
            }

            asatLimitEvaluation == CANNOT_BE_DETERMINED -> {
                val message =
                    labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedSingle(
                        createMeasurementString(ASPARTATE_AMINOTRANSFERASE)
                    )
                EvaluationFactory.recoverableUndetermined(message)
            }

            alatLimitEvaluation == CANNOT_BE_DETERMINED -> {
                val message =
                    labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedSingle(
                        createMeasurementString(ALANINE_AMINOTRANSFERASE)
                    )
                EvaluationFactory.recoverableUndetermined(message)
            }

            else -> {
                val message = labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesUndeterminedUnableToDetermine()
                EvaluationFactory.undetermined(message)
            }
        }
    }

    private fun checkValidity(
        mostRecent: LabValue?, measurement: LabMeasurement
    ): Boolean {
        return isValid(mostRecent, measurement, minValidLabDate) && mostRecent?.date?.isAfter(minPassLabDate) == true
    }

    private fun evaluateMeasurement(mostRecent: LabValue?, hasLiverMetastases: Boolean?): LabEvaluation.LabEvaluationResult {
        return if (mostRecent == null) {
            CANNOT_BE_DETERMINED
        }
        else if (hasLiverMetastases == true) {
            evaluateVersusMaxULN(mostRecent, maxULNWithLiverMetastases)
        } else {
            evaluateVersusMaxULN(mostRecent, maxULNWithoutLiverMetastases)
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
        return "${createMeasurementString(measurement)} ${String.format(ApplicationConfig.LOCALE, "%.1f", mostRecent?.value)}"
    }

    private fun evaluateOutsideMargin(measurementsWithinLimit: Boolean, hasLiverMetastases: Boolean?, message: String): Evaluation {
        return if (measurementsWithinLimit && hasLiverMetastases == null) {
            val messageEnding = labels.hasLimitedAsatAndAlatDependingOnLiverMetastasesUndeterminedSuffixUnknownLiverMetastases()
            EvaluationFactory.undetermined(message + messageEnding)
        } else {
            EvaluationFactory.recoverableFail(message)
        }
    }
}