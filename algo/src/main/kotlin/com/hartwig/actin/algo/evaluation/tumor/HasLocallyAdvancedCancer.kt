package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasLocallyAdvancedCancer : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined("Locally advanced cancer undetermined (tumor stage missing)")
        val stageIIIEvaluationResult = HasTumorStage(setOf(TumorStage.III)).evaluate(record).result
        val stageIIEvaluationResult = HasTumorStage(setOf(TumorStage.II)).evaluate(record).result

        return when {
            stageIIIEvaluationResult == EvaluationResult.PASS -> {
                EvaluationFactory.pass("Stage $stage is considered locally advanced")
            }

            stageIIEvaluationResult != EvaluationResult.FAIL || stageIIIEvaluationResult != EvaluationResult.FAIL -> {
                EvaluationFactory.undetermined("Undetermined if stage $stage is considered locally advanced")
            }

            else -> EvaluationFactory.fail("Stage $stage is not considered locally advanced")
        }
    }
}