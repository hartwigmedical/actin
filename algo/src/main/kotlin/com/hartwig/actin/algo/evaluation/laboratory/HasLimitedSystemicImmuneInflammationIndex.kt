package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate
import kotlin.math.roundToInt

class HasLimitedSystemicImmuneInflammationIndex(
    private val index: Double,
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpreter = LabInterpretation.interpret(record.labValues)
        val neutrophils = interpreter.mostRecentValue(LabMeasurement.NEUTROPHILS_ABS)
        val thrombocytes = interpreter.mostRecentValue(LabMeasurement.THROMBOCYTES_ABS)
        val lymphocytes = interpreter.mostRecentValue(LabMeasurement.LYMPHOCYTES_ABS)

        val invalidLabValue = LabEvaluation.firstInvalidLabValue(
            minValidLabDate,
            neutrophils to LabMeasurement.NEUTROPHILS_ABS,
            thrombocytes to LabMeasurement.THROMBOCYTES_ABS,
            lymphocytes to LabMeasurement.LYMPHOCYTES_ABS
        )

        val calculatedIndex = calculateSystemicImmuneInflammationIndex(neutrophils!!, thrombocytes!!, lymphocytes!!)
            ?: return EvaluationFactory.recoverableUndetermined(
                "Systemic immune-inflammation index cannot be calculated since neutrophils and/or thrombocytes and/or lymphocytes " +
                        "not in expected unit and not able to convert"
            )

        return when {
            invalidLabValue != null -> invalidLabValue

            calculatedIndex <= index -> {
                val message = "Systemic immune-inflammation index at most $index" +
                        if (neutrophils.date.isBefore(minPassLabDate) || thrombocytes.date.isBefore(minPassLabDate) ||
                            lymphocytes.date.isBefore(minPassLabDate)
                        ) {
                            " but measurement occurred before $minPassLabDate"
                        } else {
                            ""
                        }
                EvaluationFactory.recoverablePass(message)
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Systemic immune-inflammation index (${calculatedIndex.roundToInt()}) above $index"
                )
            }
        }
    }

    private fun calculateSystemicImmuneInflammationIndex(neutrophils: LabValue, thrombocytes: LabValue, lymphocytes: LabValue): Double? {
        val convertedNeutrophils = LabUnitConverter.convert(LabMeasurement.NEUTROPHILS_ABS, neutrophils, LabUnit.BILLIONS_PER_LITER)
        val convertedThrombocytes = LabUnitConverter.convert(LabMeasurement.THROMBOCYTES_ABS, thrombocytes, LabUnit.BILLIONS_PER_LITER)
        val convertedLymphocytes = LabUnitConverter.convert(LabMeasurement.LYMPHOCYTES_ABS, lymphocytes, LabUnit.BILLIONS_PER_LITER)
        return if (convertedNeutrophils != null && convertedThrombocytes != null && convertedLymphocytes != null) {
            (convertedNeutrophils * convertedThrombocytes) / convertedLymphocytes
        } else null
    }
}