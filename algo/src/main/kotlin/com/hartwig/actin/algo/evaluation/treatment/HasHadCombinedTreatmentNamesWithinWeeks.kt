package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

//TODO: Implement according to README
class HasHadCombinedTreatmentNamesWithinWeeks : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Combined treatment with specific nr of weeks currently cannot be determined",
            "Undetermined combined treatment with specific nr of weeks"
        )
    }
}