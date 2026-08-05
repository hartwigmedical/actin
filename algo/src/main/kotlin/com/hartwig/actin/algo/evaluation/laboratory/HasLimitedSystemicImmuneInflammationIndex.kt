package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedSystemicImmuneInflammationIndex(
    private val index: Double,
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate,
    private val labels: EvaluationLabels.Laboratory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpreter = LabInterpretation.interpret(record.labValues)
        val neutrophils = interpreter.mostRecentValue(LabMeasurement.NEUTROPHILS_ABS)
        val thrombocytes = interpreter.mostRecentValue(LabMeasurement.THROMBOCYTES_ABS)
        val lymphocytes = interpreter.mostRecentValue(LabMeasurement.LYMPHOCYTES_ABS)

        val invalidLabValue = LabEvaluation.firstInvalidLabValue(
            minValidLabDate,
            labels,
            neutrophils to LabMeasurement.NEUTROPHILS_ABS,
            thrombocytes to LabMeasurement.THROMBOCYTES_ABS,
            lymphocytes to LabMeasurement.LYMPHOCYTES_ABS
        )

        if (invalidLabValue != null) {
            return invalidLabValue
        }

        val calculatedIndex = calculateSystemicImmuneInflammationIndex(neutrophils!!, thrombocytes!!, lymphocytes!!)
            ?: return EvaluationFactory.recoverableUndetermined(labels.hasLimitedSystemicImmuneInflammationIndexCannotCalculate())

        return if (calculatedIndex <= index) {
            val message = labels.hasLimitedSystemicImmuneInflammationIndexPass(index) +
                    if (neutrophils.date.isBefore(minPassLabDate) || thrombocytes.date.isBefore(minPassLabDate) || lymphocytes.date.isBefore(minPassLabDate)) {
                        labels.hasLimitedSystemicImmuneInflammationIndexOccurredBeforeSuffix(minPassLabDate)
                    } else {
                        ""
                    }
            EvaluationFactory.recoverablePass(message)
        } else {
            EvaluationFactory.recoverableFail(labels.hasLimitedSystemicImmuneInflammationIndexRecoverableFail(index))
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