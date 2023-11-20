package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSufficientBloodPressure(private val category: BloodPressureCategory, private val minMedianBloodPressure: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BloodPressureFuncions.evaluatePatientBloodPressureAgainstMin(record, this.category, this.minMedianBloodPressure)
    }
}