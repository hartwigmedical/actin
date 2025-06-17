package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAvailableProteinExpression(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (IhcTestFilter.allIhcTestsForProtein(record.ihcTests, protein).isNotEmpty()) {
            EvaluationFactory.recoverablePass("$protein expression available by IHC")
        } else {
            EvaluationFactory.recoverableFail("$protein expression not available by IHC")
        }
    }
}