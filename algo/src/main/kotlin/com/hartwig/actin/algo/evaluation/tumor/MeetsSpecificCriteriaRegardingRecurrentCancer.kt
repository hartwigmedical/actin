package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingRecurrentCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined("Undetermined if patient meets specific criteria regarding recurrent cancer")
    }
}