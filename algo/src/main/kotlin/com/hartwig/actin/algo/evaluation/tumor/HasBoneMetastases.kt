package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasBoneMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TumorMetastasisEvaluator.evaluate(record.tumor.hasBoneLesions, record.tumor.hasSuspectedBoneLesions, "bone")
    }
}