package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class HasSufficientBloodPressure(
    private val category: BloodPressureCategory, private val minMedianBloodPressure: Int, private val minimumDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BloodPressureFunctions.evaluatePatientMinimumBloodPressure(
            record,
            this.category,
            this.minMedianBloodPressure,
            this.minimumDate
        )
    }
}