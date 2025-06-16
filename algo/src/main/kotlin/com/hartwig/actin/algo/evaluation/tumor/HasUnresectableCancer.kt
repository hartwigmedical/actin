package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isStageMatch
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasUnresectableCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage =
            record.tumor.stage ?: return EvaluationFactory.undetermined("Undetermined if cancer is unresectable (tumor stage missing)")
        val stageMessage = stage.display()

        return when {
            isStageMatch(stage, setOf(TumorStage.IV)) -> {
                EvaluationFactory.pass("Has unresectable cancer (stage $stageMessage)")
            }

            isStageMatch(stage, setOf(TumorStage.III)) -> {
                EvaluationFactory.undetermined("Undetermined if cancer is unresectable (stage $stageMessage)")
            }

            else -> {
                EvaluationFactory.fail("No unresectable cancer (stage $stageMessage)")
            }
        }
    }
}