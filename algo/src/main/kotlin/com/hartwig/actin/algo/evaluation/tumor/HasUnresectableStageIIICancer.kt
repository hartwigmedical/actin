package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isStageMatch
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasUnresectableStageIIICancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage =
            record.tumor.stage ?: return EvaluationFactory.undetermined("Unresectable stage III cancer undetermined (tumor stage missing)")

        return if (isStageMatch(stage, setOf(TumorStage.III))) {
            EvaluationFactory.undetermined("Undetermined if stage III cancer is considered unresectable")
        } else {
            EvaluationFactory.fail("No unresectable stage III cancer")
        }
    }
}