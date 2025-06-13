package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasUnresectableCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage =
            record.tumor.stage ?: return EvaluationFactory.undetermined("Undetermined if cancer is unresectable (tumor stage missing)")
        val stageMessage =
            record.tumor.stage?.display() ?: "(derived stage: ${record.tumor.derivedStages?.joinToString(" or ") { it.display() }})"

        return when {
            (stage.category ?: stage) == TumorStage.IV -> {
                EvaluationFactory.pass("Has unresectable cancer (stage $stageMessage)")
            }

            (stage.category ?: stage) == TumorStage.III -> {
                EvaluationFactory.undetermined("Undetermined if cancer is unresectable (stage $stageMessage)")
            }

            else -> {
                EvaluationFactory.fail("No unresectable cancer (stage $stageMessage)")
            }
        }
    }
}