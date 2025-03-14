package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.UnparameterisedIhcRule
import com.hartwig.actin.molecular.UnparameterisedIhcRule.Companion.PDL1

@UnparameterisedIhcRule(PDL1)
class HasSufficientPDL1ByIHC(private val measure: String?, private val minPDL1: Double, private val doidModel: DoidModel? = null) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return PDL1EvaluationFunctions.evaluatePDL1byIHC(record, measure, minPDL1, doidModel, evaluateMaxPDL1 = false)
    }
}