package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.isValid
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

class HasAbnormalElectrolyteLevels(private val minValidLabDate: LocalDate, private val minPassLabDate: LocalDate): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val measurements = listOf(
            LabMeasurement.CALCIUM,
            LabMeasurement.PHOSPHORUS,
            LabMeasurement.SODIUM,
            LabMeasurement.MAGNESIUM,
            LabMeasurement.POTASSIUM
        )

        val evaluations = measurements.map { evaluateMeasurement(record, it) }
        val outsideRefEvaluations = evaluations.filter { it?.second == true }
        val measurementString = outsideRefEvaluations.map { it?.first }.joinToString(",")

        return when {
            outsideRefEvaluations.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has abnormal electrolyte levels ($measurementString outside reference range)",
                    "Electrolyte levels abnormal ($measurementString outside reference range)"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient does not have abnormal electrolyte levels", "Electrolyte levels within reference range")
            }
        }
    }

    private fun evaluateMeasurement(record: PatientRecord, measurement: LabMeasurement): Pair<LabMeasurement, Boolean?>? {
        val mostRecent = LabInterpreter.interpret(record.labValues).mostRecentValue(measurement)
        val valid = isValid(mostRecent, measurement, minValidLabDate) && mostRecent?.date?.isAfter(minPassLabDate) == true
        return if (valid) Pair(measurement, mostRecent?.isOutsideRef) else null
    }
}