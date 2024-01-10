package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import java.time.LocalDate

class HasLimitedBloodPressure(
    private val category: BloodPressureCategory, private val maxMedianBloodPressure: Int, private val minimalDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BloodPressureFunctions.evaluatePatientMaximumBloodPressure(
            record, this.category, this.maxMedianBloodPressure, this.minimalDate
        )
    }
}