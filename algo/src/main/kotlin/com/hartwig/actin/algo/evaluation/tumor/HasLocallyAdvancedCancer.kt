package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isStageMatch
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasLocallyAdvancedCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage =
            record.tumor.stage ?: return EvaluationFactory.undetermined("Undetermined if locally advanced cancer (tumor stage missing)")
        val stageMessage = stage.display()

        return when {
            isStageMatch(stage, setOf(TumorStage.III)) -> {
                EvaluationFactory.pass("Stage $stageMessage is considered locally advanced")
            }

            isStageMatch(stage, setOf(TumorStage.II)) -> {
                EvaluationFactory.undetermined("Undetermined if stage $stageMessage is considered locally advanced")
            }

            else -> EvaluationFactory.fail("Stage $stageMessage is not considered locally advanced")
        }
    }
}