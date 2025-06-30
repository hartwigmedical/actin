package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.trial.input.datamodel.AlbiGrade
import java.time.LocalDate
import kotlin.math.log10

class HasSpecificAlbiGrade(
    private val grade: AlbiGrade, private val minValidDate: LocalDate, private val minPassDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (albumin, bilirubin) = listOf(LabMeasurement.ALBUMIN, LabMeasurement.TOTAL_BILIRUBIN)
            .map { measurement -> record.labValues.filter { it.measurement == measurement } }
            .map { it.sortedWith(LabValueDescendingDateComparator(true)).firstOrNull() }

        val albuminValid = LabEvaluation.isValid(albumin, LabMeasurement.ALBUMIN, minValidDate, LabUnit.GRAMS_PER_LITER)
        val bilirubinValid = LabEvaluation.isValid(bilirubin, LabMeasurement.TOTAL_BILIRUBIN, minValidDate, LabUnit.MICROMOLES_PER_LITER)

        return when {
            !albuminValid -> {
                LabEvaluation.evaluateInvalidLabValue(LabMeasurement.ALBUMIN, albumin, minValidDate, LabUnit.GRAMS_PER_LITER)
            }

            !bilirubinValid -> {
                LabEvaluation.evaluateInvalidLabValue(LabMeasurement.TOTAL_BILIRUBIN, bilirubin, minValidDate, LabUnit.MICROMOLES_PER_LITER)
            }

            else -> {
                val albiScore = calculateAlbiScore(albumin!!, bilirubin!!)
                val albiGrade = when {
                    albiScore <= -2.60 -> AlbiGrade.GRADE_1
                    albiScore <= -1.39 -> AlbiGrade.GRADE_2
                    else -> AlbiGrade.GRADE_3
                }

                if (albiGrade == grade) {
                    val message = "Albumin-bilirubin (ALBI) grade sufficient." +
                            if (albumin.date.isBefore(minPassDate) || bilirubin.date.isBefore(minPassDate)) {
                        " but measurement occurred before $minPassDate"
                    } else {
                        ""
                    }
                    EvaluationFactory.recoverablePass(message)
                } else {
                    EvaluationFactory.recoverableFail(
                        "Albumin-bilirubin (ALBI) grade ($albiGrade) insufficient - should be ${grade.display}."
                    )
                }
            }
        }
    }

    private fun calculateAlbiScore(albumin: LabValue, bilirubin: LabValue): Double {
        return log10(bilirubin.value) * 0.66 - 0.0852 * albumin.value
    }
}