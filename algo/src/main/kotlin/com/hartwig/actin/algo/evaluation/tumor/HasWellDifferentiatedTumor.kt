package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasWellDifferentiatedTumor : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            WELL_DIFFERENTIATED_TERMS.any {
                record.tumor.name.lowercase().contains(it)
            } -> EvaluationFactory.pass("Tumor is well-differentiated")

            OTHER_DIFFERENTIATION_TERMS.any {
                record.tumor.name.lowercase().contains(it)
            } -> EvaluationFactory.fail("Tumor is not well-differentiated")

            else -> EvaluationFactory.undetermined("Undetermined if well-differentiated tumor")
        }
    }

    companion object {
        val WELL_DIFFERENTIATED_TERMS = setOf("well-differentiated", "well differentiated")
        val OTHER_DIFFERENTIATION_TERMS = setOf("moderately differentiated", "poorly differentiated", "undifferentiated")
    }
}