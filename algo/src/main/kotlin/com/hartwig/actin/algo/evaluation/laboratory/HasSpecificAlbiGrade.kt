package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
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
    private val minPassLabDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpreter = LabInterpretation.interpret(record.labValues)
        val bilirubin = interpreter.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN)
        val albumin = interpreter.mostRecentValue(LabMeasurement.ALBUMIN)

        return when {
            !LabEvaluation.isValid(albumin, LabMeasurement.ALBUMIN, minValidLabDate) -> {
                LabEvaluation.evaluateInvalidLabValue(LabMeasurement.ALBUMIN, albumin, minValidLabDate)
            }

            !LabEvaluation.isValid(bilirubin, LabMeasurement.TOTAL_BILIRUBIN, minValidLabDate) -> {
                LabEvaluation.evaluateInvalidLabValue(LabMeasurement.TOTAL_BILIRUBIN, bilirubin, minValidLabDate)
            }

            else -> {
                val albiScore = calculateAlbiScore(albumin!!, bilirubin!!)

                if (albiScore == null) {
                    EvaluationFactory.recoverableUndetermined(
                        "ALBI score cannot be calculated since albumin or bilirubin not in expected unit and not able to convert"
                    )
                }

                val albiGrade = when {
                    albiScore!! <= -2.60 -> AlbiGrade.GRADE_1
                    albiScore <= -1.39 -> AlbiGrade.GRADE_2
                    else -> AlbiGrade.GRADE_3
                }

                if (albiGrade == grade) {
                    val message = "ALBI grade sufficient" +
                            if (albumin.date.isBefore(minPassLabDate) || bilirubin.date.isBefore(minPassLabDate)) {
                        " but measurement occurred before $minPassLabDate"
                    } else {
                        ""
                    }
                    EvaluationFactory.recoverablePass(message)
                } else {
                    EvaluationFactory.recoverableFail(
                        "ALBI grade (${albiGrade.display()}) insufficient - should be ${grade.display()}"
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