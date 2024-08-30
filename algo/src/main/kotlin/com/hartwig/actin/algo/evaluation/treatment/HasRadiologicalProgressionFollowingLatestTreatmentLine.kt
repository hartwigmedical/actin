package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

//TODO: Implement according to README
class HasRadiologicalProgressionFollowingLatestTreatmentLine : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndetermined(
            "Radiological progression following latest treatment line currently cannot be evaluated",
            "Undetermined radiological progression following latest treatment line"
        )
    }
}