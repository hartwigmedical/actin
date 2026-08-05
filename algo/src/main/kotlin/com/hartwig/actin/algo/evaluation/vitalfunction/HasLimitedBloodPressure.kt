package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class HasLimitedBloodPressure(
    private val category: BloodPressureCategory,
    private val maxMedianBloodPressure: Int,
    private val minimumDate: LocalDate,
    private val labels: EvaluationLabels.VitalFunction
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BloodPressureFunctions.evaluatePatientMaximumBloodPressure(
            record, this.category, this.maxMedianBloodPressure, this.minimumDate, labels
        )
    }
}