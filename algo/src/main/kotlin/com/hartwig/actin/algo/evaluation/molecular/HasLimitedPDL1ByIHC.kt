package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasLimitedPDL1ByIHC(
    private val measure: String?, private val maxPDL1: Double, private val doidModel: DoidModel? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return PDL1EvaluationFunctions.evaluateLimitedPDL1byIHC(record, measure, maxPDL1, doidModel)
    }
}