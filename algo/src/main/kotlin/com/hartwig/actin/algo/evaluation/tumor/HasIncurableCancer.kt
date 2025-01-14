package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasIncurableCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage
            ?: return EvaluationFactory.undetermined("Incurable cancer undetermined (tumor stage missing)")

        return when {
            isStageMatch(stage, TumorStage.IV) -> {
                EvaluationFactory.pass("Stage IV cancer is considered incurable")
            }

            isStageMatch(stage, TumorStage.III) -> {
                EvaluationFactory.undetermined("Undetermined if stage $stage is considered incurable")
            }

            else -> {
                EvaluationFactory.fail("Stage $stage cancer is not considered incurable")
            }
        }
    }

    companion object {
        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }
    }
}