package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasLowGradeCancer() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLowGrade = LOW_GRADE_TERMS.any { record.tumor.name.lowercase().contains(it) }
        val hasHighGrade = HIGH_GRADE_TERMS.any { record.tumor.name.lowercase().contains(it) }

        return when {
            hasLowGrade -> EvaluationFactory.pass("Has low grade cancer")
            hasHighGrade -> EvaluationFactory.fail("Has high grade cancer")
            else -> EvaluationFactory.undetermined("Undetermined if low or high grade cancer")
        }
    }

    companion object {
        val LOW_GRADE_TERMS = setOf("low grade", "low-grade")
        val HIGH_GRADE_TERMS = setOf("high grade", "high-grade")
    }
}