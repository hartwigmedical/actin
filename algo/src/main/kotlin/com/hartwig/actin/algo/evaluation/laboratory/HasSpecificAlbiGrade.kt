package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AlbiGrade
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate
import kotlin.math.log10

class HasSpecificAlbiGrade(
    private val grade: AlbiGrade,
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate,
    private val labels: EvaluationLabels.Laboratory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpreter = LabInterpretation.interpret(record.labValues)
        val bilirubin = interpreter.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        val albumin = interpreter.mostRecentValue(LabMeasurement.ALBUMIN)

        return when {
            !LabEvaluation.isValid(albumin, LabMeasurement.ALBUMIN, minValidLabDate) -> {
                LabEvaluation.evaluateInvalidLabValue(LabMeasurement.ALBUMIN, albumin, minValidLabDate, labels)
            }

            !LabEvaluation.isValid(bilirubin, LabMeasurement.TOTAL_BILIRUBIN, minValidLabDate) -> {
                LabEvaluation.evaluateInvalidLabValue(LabMeasurement.TOTAL_BILIRUBIN, bilirubin, minValidLabDate, labels)
            }

            else -> {
                val albiScore = calculateAlbiScore(albumin!!, bilirubin!!)

                if (albiScore == null) {
                    EvaluationFactory.recoverableUndetermined(labels.hasSpecificAlbiGradeCannotCalculate())
                }

                val albiGrade = when {
                    albiScore!! <= -2.60 -> AlbiGrade.GRADE_1
                    albiScore <= -1.39 -> AlbiGrade.GRADE_2
                    else -> AlbiGrade.GRADE_3
                }

                if (albiGrade == grade) {
                    val message = labels.hasSpecificAlbiGradePass() +
                            if (albumin.date.isBefore(minPassLabDate) || bilirubin.date.isBefore(minPassLabDate)) {
                        labels.hasSpecificAlbiGradeOccurredBeforeSuffix(minPassLabDate)
                    } else {
                        ""
                    }
                    EvaluationFactory.recoverablePass(message)
                } else {
                    EvaluationFactory.recoverableFail(
                        labels.hasSpecificAlbiGradeRecoverableFail(albiGrade.display(), grade.display())
                    )
                }
            }
        }
    }

    private fun calculateAlbiScore(albumin: LabValue, bilirubin: LabValue): Double? {
        val convertedBilirubin =
            LabUnitConverter.convert(LabMeasurement.TOTAL_BILIRUBIN, bilirubin, LabUnit.MICROMOLES_PER_LITER)
        val convertedAlbumin = LabUnitConverter.convert(LabMeasurement.ALBUMIN, albumin, LabUnit.GRAMS_PER_LITER)
        return if (convertedBilirubin != null && convertedAlbumin != null) {
            log10(convertedBilirubin) * 0.66 - 0.0852 * convertedAlbumin
        } else null
    }
}