package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHistoryOfAnaphylaxis internal constructor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.intolerances.isEmpty()) {
            EvaluationFactory.fail("Patient has no known history of anaphylaxis", "No known history of anaphylaxis")
        } else {
            EvaluationFactory.undetermined(
                "Drug allergies present but cannot be determined if patient has history of anaphylaxis",
                "Drug allergies present but unknown if history of anaphylaxis"
            )
        }
    }
}