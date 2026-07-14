package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasWellDifferentiatedTumor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isWellDifferentiatedType = WELL_DIFFERENTIATED_TERMS.any { record.tumor.name.lowercase().contains(it) }
        val isOtherDifferentiatedType = OTHER_DIFFERENTIATION_TERMS.any { record.tumor.name.lowercase().contains(it) }

        return when {
            isWellDifferentiatedType -> EvaluationFactory.pass("Has well-differentiated tumor")
            isOtherDifferentiatedType -> EvaluationFactory.fail("Has no well-differentiated tumor")
            else -> EvaluationFactory.warn("Undetermined if well-differentiated tumor")
        }
    }

    companion object {
        val WELL_DIFFERENTIATED_TERMS = setOf("well-differentiated", "well differentiated")
        val OTHER_DIFFERENTIATION_TERMS = setOf("moderately differentiated", "poorly differentiated", "undifferentiated")
    }
}