package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

//TODO: Implement according to README
class HasHadCombinedTreatmentNamesWithinWeeks : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Combined treatment with specific nr of weeks currently cannot be determined",
            "Undetermined combined treatment with specific nr of weeks"
        )
    }
}