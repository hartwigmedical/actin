package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

//TODO (CB)!
class HasUnresectableCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined(
            "Undetermined if cancer is unresectable (tumor stage missing)"
        )

        return when {
            isStageMatch(stage, TumorStage.IV) -> {
                EvaluationFactory.pass("Has unresectable cancer")
            }

            isStageMatch(stage, TumorStage.III) -> {
                EvaluationFactory.undetermined("Undetermined if cancer is unresectable")
            }

            else -> {
                EvaluationFactory.fail("No unresectable cancer")
            }
        }
    }

    companion object {
        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }
    }
}