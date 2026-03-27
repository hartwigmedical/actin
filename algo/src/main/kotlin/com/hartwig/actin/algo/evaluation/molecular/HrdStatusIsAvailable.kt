package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HrdStatusIsAvailable : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.molecularTests.any { it.characteristics.homologousRecombination != null }) {
            EvaluationFactory.pass("HRD status is available")
        } else {
            EvaluationFactory.recoverableFail("No HRD status available")
        }
    }
}