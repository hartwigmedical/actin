package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails

class HasLiverMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TumorMetastasisEvaluator.evaluate(
            record.tumor.hasLiverLesions, record.tumor.hasSuspectedLiverLesions, TumorDetails.LIVER.lowercase()
        )
    }
}