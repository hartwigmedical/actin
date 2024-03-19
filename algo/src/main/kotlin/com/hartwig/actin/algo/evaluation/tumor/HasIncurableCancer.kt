package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorStage

class HasIncurableCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage
            ?: return EvaluationFactory.undetermined(
                "Tumor stage details are missing, if cancer is considered incurable cannot be determined",
                "Undetermined incurable cancer"
            )

        return when {
            isStageMatch(stage, TumorStage.IV) -> {
                EvaluationFactory.pass("Stage IV cancer is considered incurable", "Incurable cancer")
            }

            isStageMatch(stage, TumorStage.III) -> {
                EvaluationFactory.undetermined(
                    "Could not be determined if stage $stage cancer is considered incurable",
                    "Undetermined if cancer is incurable by stage $stage"
                )
            }

            else -> {
                EvaluationFactory.fail("Stage $stage cancer is not considered incurable", "No incurable cancer")
            }
        }
    }

    companion object {
        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }
    }
}