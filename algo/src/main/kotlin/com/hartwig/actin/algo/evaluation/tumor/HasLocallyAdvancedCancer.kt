package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasLocallyAdvancedCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage =
            record.tumor.stage ?: return EvaluationFactory.undetermined("Locally advanced cancer undetermined (tumor stage missing)")
        return if (isStageMatch(stage, TumorStage.III)) {
            EvaluationFactory.pass("Stage $stage is considered locally advanced")
        } else if (isStageMatch(stage, TumorStage.II)) {
            EvaluationFactory.warn("Unclear if stage $stage is considered locally advanced")
        } else {
            EvaluationFactory.fail("Stage $stage is not considered locally advanced")
        }
    }

    companion object {
        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }
    }
}