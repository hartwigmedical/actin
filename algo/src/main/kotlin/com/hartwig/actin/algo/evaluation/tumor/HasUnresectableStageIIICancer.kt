package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isStageMatch
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasUnresectableStageIIICancer(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage =
            record.tumor.stage ?: return EvaluationFactory.undetermined(labels.hasUnresectableStageIIICancerUndeterminedMissing())

        return if (isStageMatch(stage, setOf(TumorStage.III))) {
            EvaluationFactory.undetermined(labels.hasUnresectableStageIIICancerUndetermined())
        } else {
            EvaluationFactory.fail(labels.hasUnresectableStageIIICancerFail())
        }
    }
}