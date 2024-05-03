package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorStage

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
                pass(
                    "No tumor stage details present but $derivedStageMessage",
                    "Missing tumor stage details - $derivedStageMessage"
                )
            } else if (derivedStages?.map { evaluateWithStage(it, true) }?.any { it.result == EvaluationResult.PASS } == true) {
                val stageMessage = stagesToMatch.joinToString(" or ") { it.display() }
                val derivedStageMessage = "derived ${derivedStages.joinToString(" or ") { it.display() }} based on lesions"
                undetermined(
                    "Unknown if tumor stage is $stageMessage (no tumor stage details provided) - $derivedStageMessage",
                    "Unknown if tumor stage is $stageMessage (data missing) - $derivedStageMessage"
                )
            } else {
                fail(
                    "No tumor stage details present but based on lesions requested stage cannot be met",
                    "Tumor stage unknown but requested stage not met based on lesions"
                )
            }
        }
        return evaluateWithStage(stage, false)
    }

    private fun evaluateWithStage(stage: TumorStage, derived: Boolean): Evaluation {
        val stageString = stagesToMatch.joinToString(" or ") { it.display() }
        return if (stage in stagesToMatch || stage.category in stagesToMatch || (derived && evaluateCategoryMatchForDerivedStage(stage))) {
            pass("Patient tumor stage is requested stage $stageString", "Adequate tumor stage")
        } else {
            fail("Patient tumor stage is not requested stage $stageString", "Inadequate tumor stage")
        }
    }

    private fun evaluateCategoryMatchForDerivedStage(derivedStage: TumorStage): Boolean {
        val stagesInCategory =
            derivedStage.category?.let { TumorStage.values().filter { it.category != null && it.category == it } } ?: emptySet()
        return derivedStage in stagesInCategory
    }
}