package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasLocallyAdvancedCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage
            ?: return EvaluationFactory.undetermined(
                "Tumor stage details are missing, if cancer is locally advanced cannot be determined",
                "Undetermined locally advanced cancer"
            )
        return if (isStageMatch(stage, TumorStage.III)) {
            EvaluationFactory.pass("Tumor stage $stage is considered locally advanced", "Locally advanced cancer")
        } else if (isStageMatch(stage, TumorStage.II)) {
            EvaluationFactory.warn(
                "Could not be determined if tumor stage $stage is considered locally advanced",
                "Unclear if locally advanced cancer for stage $stage"
            )
        } else {
            EvaluationFactory.fail("Tumor stage $stage is not considered locally advanced", "No locally advanced cancer")
        }
    }

    companion object {
        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }
    }
}