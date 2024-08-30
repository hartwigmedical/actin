package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class HasLimitedBodyWeight(private val maximumWeight: Double, private val minimumDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BodyWeightFunctions.evaluatePatientForMaximumBodyWeight(record, this.maximumWeight, this.minimumDate)
    }
}

