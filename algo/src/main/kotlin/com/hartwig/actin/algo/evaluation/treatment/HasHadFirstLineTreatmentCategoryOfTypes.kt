package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

//TODO: Implement patient has had treatment of category X according to 2] below, and corresponding type in Y as first treatment line. If a surgery is present and the line is the first line after surgery, resolve to `UNDETERMINED`
class HasHadFirstLineTreatmentCategoryOfTypes : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined("Undetermined if certain treatment was given as first-line treatment")
    }
}