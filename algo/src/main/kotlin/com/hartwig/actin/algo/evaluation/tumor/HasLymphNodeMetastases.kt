package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.doid.DoidModel

class HasLymphNodeMetastases(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TumorMetastasisEvaluator.evaluate(
            record.tumor.hasLymphNodeLesions,
            record.tumor.hasSuspectedLymphNodeLesions,
            TumorDetails.LYMPH_NODE,
            record.tumor.doids,
            doidModel
        )
    }
}