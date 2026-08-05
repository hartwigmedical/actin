package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasWellDifferentiatedTumor(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            WELL_DIFFERENTIATED_TERMS.any {
                record.tumor.name.lowercase().contains(it)
            } -> EvaluationFactory.pass(labels.hasWellDifferentiatedTumorPass())

            OTHER_DIFFERENTIATION_TERMS.any {
                record.tumor.name.lowercase().contains(it)
            } -> EvaluationFactory.fail(labels.hasWellDifferentiatedTumorFail())

            else -> EvaluationFactory.undetermined(labels.hasWellDifferentiatedTumorUndetermined())
        }
    }

    companion object {
        val WELL_DIFFERENTIATED_TERMS = setOf("well-differentiated", "well differentiated")
        val OTHER_DIFFERENTIATION_TERMS = setOf("moderately differentiated", "poorly differentiated", "undifferentiated")
    }
}