package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasLocallyAdvancedCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined("Undetermined if locally advanced cancer (tumor stage missing)")

        return when {
            (stage.category ?: stage) == TumorStage.III -> {
                EvaluationFactory.pass("Stage ${stage.display()} is considered locally advanced")
            }

            (stage.category ?: stage) == TumorStage.II -> {
                EvaluationFactory.undetermined("Undetermined if stage ${stage.display()} is considered locally advanced")
            }

            else -> EvaluationFactory.fail("Stage ${stage.display()} is not considered locally advanced")
        }
    }
}