package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.molecular.UnparameterisedIhcRule
import com.hartwig.actin.molecular.UnparameterisedIhcRule.Companion.PDL1

@UnparameterisedIhcRule(PDL1)
class HasAvailablePDL1Status : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (IhcTestFilter.allPDL1Tests(record.priorIHCTests).isNotEmpty()) {
            EvaluationFactory.recoverablePass("PD-L1 status available")
        } else {
            EvaluationFactory.recoverableFail("PD-L1 status not available")
        }
    }
}