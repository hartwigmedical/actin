package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.isValid
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate

class HasAbnormalElectrolyteLevels(
    private val minValidLabDate: LocalDate,
    private val minPassLabDate: LocalDate,
    private val labels: EvaluationLabels.Laboratory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val measurements = listOf(
            LabMeasurement.CALCIUM,
            LabMeasurement.CHLORIDE,
            LabMeasurement.BICARBONATE,
            LabMeasurement.PHOSPHATE,
            LabMeasurement.SODIUM,
            LabMeasurement.MAGNESIUM,
            LabMeasurement.POTASSIUM
        )

        val outsideRef = measurements.mapNotNull { returnOutsideRefMeasurements(record, it) }
        val measurementString = outsideRef.joinToString(",") { it.display }

        return when {
            outsideRef.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(labels.hasAbnormalElectrolyteLevelsPass(measurementString))
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.hasAbnormalElectrolyteLevelsRecoverableFail())
            }
        }
    }

    private fun returnOutsideRefMeasurements(record: PatientRecord, measurement: LabMeasurement): LabMeasurement? {
        val mostRecent = LabInterpretation.interpret(record.labValues).mostRecentValue(measurement)
        val valid = isValid(mostRecent, measurement, minValidLabDate) && mostRecent?.date?.isAfter(minPassLabDate) == true
        return measurement.takeIf { valid && mostRecent!!.isOutsideRef == true }
    }
}