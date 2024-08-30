package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHistoryOfAnaphylaxis: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.intolerances.isEmpty()) {
            EvaluationFactory.fail("Patient has no known history of anaphylaxis", "No known history of anaphylaxis")
        } else {
            EvaluationFactory.undetermined(
                "Allergies present but cannot be determined if patient has history of anaphylaxis",
                "Allergies present but unknown if history of anaphylaxis"
            )
        }
    }
}