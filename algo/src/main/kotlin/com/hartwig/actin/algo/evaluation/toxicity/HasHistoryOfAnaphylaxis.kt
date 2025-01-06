package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHistoryOfAnaphylaxis: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.intolerances.isEmpty()) {
            EvaluationFactory.fail("No known history of anaphylaxis")
        } else {
            EvaluationFactory.undetermined("History of anaphylaxis undetermined (allergies present)")
        }
    }
}