package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasUnresectableStageIIICancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage
            ?: return EvaluationFactory.undetermined(
                "Tumor stage details are missing, if cancer is unresectable stage III cannot be determined",
                "Undetermined unresectable stage III cancer"
            )

        return if (isStageMatch(stage, TumorStage.III)) {
            EvaluationFactory.undetermined(
                "Undetermined if stage III cancer is considered unresectable",
                "Undetermined if cancer is unresectable stage III"
            )
        } else {
            EvaluationFactory.fail("Patient has no unresectable stage III cancer", "No unresectable stage III cancer")
        }
    }

    companion object {
        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }
    }
}