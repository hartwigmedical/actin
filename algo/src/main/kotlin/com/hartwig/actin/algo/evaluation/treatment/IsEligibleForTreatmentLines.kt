package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class IsEligibleForTreatmentLines(private val lines: List<Int>) : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        val nextTreatmentLine = SystemicTreatmentAnalyser.minSystemicTreatments(record.oncologicalHistory) + 1
        val message = "Patient determined to be eligible for line $nextTreatmentLine"

        return if (nextTreatmentLine in lines) EvaluationFactory.pass(message) else EvaluationFactory.fail(message)
    }
}