package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class HasSufficientBodyWeight(
    private val minimumWeight: Double, private val minimumDate: LocalDate, private val labels: EvaluationLabels.VitalFunction
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return BodyWeightFunctions.evaluatePatientForMinimumBodyWeight(record, this.minimumWeight, this.minimumDate, labels)
    }
}