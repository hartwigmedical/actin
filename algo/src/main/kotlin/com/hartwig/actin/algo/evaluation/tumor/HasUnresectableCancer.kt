package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasUnresectableCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined(
            "Tumor stage details are missing, if cancer is unresectable cannot be determined", "Undetermined unresectable cancer"
        )

        return when {
            isStageMatch(stage, TumorStage.IV) -> {
                EvaluationFactory.pass("Tumor stage $stage is considered unresectable", "Unresectable cancer")
            }
            isStageMatch(stage, TumorStage.III) -> {
                EvaluationFactory.undetermined(
                    "Tumor stage $stage is not unclear whether unresectable",
                    "Unclear if cancer is unresectable"
                )
            }
            else -> {
                EvaluationFactory.fail("Tumor stage $stage is not considered unresectable", "No unresectable cancer")
            }
        }
    }

    companion object {
        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }
    }
}