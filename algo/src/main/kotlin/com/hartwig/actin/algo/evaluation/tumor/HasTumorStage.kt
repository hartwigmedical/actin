package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasTumorStage(private val stagesToMatch: Set<TumorStage>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (stagesToMatch.isEmpty()) throw IllegalStateException("No stages to match configured")
        val stage = record.tumor.stage
        if (stage == null) {
            val derivedStages = record.tumor.derivedStages
            return if (derivedStages?.size == 1) {
                evaluateWithStage(derivedStages.iterator().next(), true)
            } else if (derivedStages?.map { evaluateWithStage(it, true) }?.all { it.result == EvaluationResult.PASS } == true) {
                val derivedStageMessage = "passes with derived ${derivedStages.joinToString(" or ") { it.display() }} based on lesions"
                pass("Missing tumor stage details - $derivedStageMessage")
            } else if (derivedStages?.map { evaluateWithStage(it, true) }?.any { it.result == EvaluationResult.PASS } == true) {
                val stageMessage = stagesToMatch.joinToString(" or ") { it.display() }
                val derivedStageMessage = "derived ${derivedStages.joinToString(" or ") { it.display() }} based on lesions"
                undetermined("Unknown if tumor stage is $stageMessage (data missing) - $derivedStageMessage")
            } else {
                fail("Tumor stage unknown but requested stage not met based on lesions")
            }
        }
        return evaluateWithStage(stage, false)
    }

    private fun evaluateWithStage(stage: TumorStage, derived: Boolean): Evaluation {
        val stageString = stagesToMatch.joinToString(" or ") { it.display() }
        return if (stage in stagesToMatch || stage.category in stagesToMatch || (derived && evaluateCategoryMatchForDerivedStage(stage))) {
            pass("Tumor stage is requested stage $stageString")
        } else {
            fail("Tumor stage is not requested stage $stageString")
        }
    }

    private fun evaluateCategoryMatchForDerivedStage(derivedStage: TumorStage): Boolean {
        return stagesToMatch.any { it.category == derivedStage }
    }
}