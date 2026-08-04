package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAvailablePDL1Status(private val labels: EvaluationLabels.Molecular) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(record.ihcTests, "PD-L1").isNotEmpty()) {
            EvaluationFactory.recoverablePass(labels.hasAvailablePdl1StatusPass())
        } else {
            EvaluationFactory.recoverableFail(labels.hasAvailablePdl1StatusRecoverableFail())
        }
    }
}