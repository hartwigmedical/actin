package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import java.time.LocalDate

class HasSufficientBodyWeight(private val minimumWeight: Double, private val minimumDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BodyWeightFunctions.evaluatePatientForMinimumBodyWeight(record, this.minimumWeight, this.minimumDate)
    }
}