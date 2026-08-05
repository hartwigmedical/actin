package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isStageMatch
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasLocallyAdvancedCancer(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage =
            record.tumor.stage ?: return EvaluationFactory.undetermined(labels.hasLocallyAdvancedCancerUndeterminedStageMissing())
        val stageMessage = stage.display()

        return when {
            isStageMatch(stage, setOf(TumorStage.III)) -> {
                EvaluationFactory.pass(labels.hasLocallyAdvancedCancerPass(stageMessage))
            }

            isStageMatch(stage, setOf(TumorStage.II)) -> {
                EvaluationFactory.undetermined(labels.hasLocallyAdvancedCancerUndetermined(stageMessage))
            }

            else -> EvaluationFactory.fail(labels.hasLocallyAdvancedCancerFail(stageMessage))
        }
    }
}