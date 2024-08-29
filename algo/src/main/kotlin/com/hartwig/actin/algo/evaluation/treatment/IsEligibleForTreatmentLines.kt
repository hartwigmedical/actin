package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class IsEligibleForTreatmentLines(private val lines: List<Int>) : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        val nextTreatmentLine = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory) + 1
        val message = "Patient determined to be eligible for line $nextTreatmentLine"

        return if (nextTreatmentLine in lines) EvaluationFactory.pass(message) else EvaluationFactory.fail(message)
    }
}