package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

//TODO: Implement Lesions or biopsyLocation like %subcutaneous% or %skin%, otherwise resolve to `UNDETERMINED`
class HasInjectionAmenableLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined("Injection amenability of lesions undetermined")
    }
}