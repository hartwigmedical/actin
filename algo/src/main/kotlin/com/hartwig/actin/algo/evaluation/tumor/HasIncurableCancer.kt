package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasIncurableCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined("Incurable cancer undetermined (tumor stage missing)")
        val stageMessage =
            record.tumor.stage?.display() ?: "(derived stage: ${record.tumor.derivedStages?.joinToString(" or ") { it.display() }})"

        return when {
            (stage.category ?: stage) == TumorStage.IV -> {
                EvaluationFactory.pass("Stage $stageMessage cancer is considered incurable")
            }

            (stage.category ?: stage) == TumorStage.III -> {
                EvaluationFactory.undetermined("Undetermined if stage $stageMessage is considered incurable")
            }

            else -> {
                EvaluationFactory.fail("Stage $stageMessage cancer is not considered incurable")
            }
        }
    }
}