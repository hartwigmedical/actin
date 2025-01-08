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
        val adjustedStagesToMatch = adjustStagesToMatch(stagesToMatch)
        val stage = record.tumor.stage

        if (stage == null) {
            val derivedStages = record.tumor.derivedStages
            return if (derivedStages?.size == 1) {
                evaluateWithStage(derivedStages.iterator().next(), adjustedStagesToMatch)
            } else if (derivedStages?.map { evaluateWithStage(it, adjustedStagesToMatch) }
                    ?.all { it.result == EvaluationResult.PASS } == true) {
                val derivedStageMessage = "passes with derived ${derivedStages.joinToString(" or ") { it.display() }} based on lesions"
                pass(
                    "No tumor stage details present but $derivedStageMessage",
                    "Missing tumor stage details - $derivedStageMessage"
                )
            } else if (derivedStages?.map { evaluateWithStage(it, adjustedStagesToMatch) }
                    ?.any { it.result in listOf(EvaluationResult.PASS, EvaluationResult.UNDETERMINED) } == true) {
                val stageMessage = adjustedStagesToMatch.joinToString(" or ") { it.display() }
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
        return evaluateWithStage(stage, adjustedStagesToMatch)
    }

    private fun evaluateWithStage(stage: TumorStage, stagesToMatch: Set<TumorStage>): Evaluation {
        val stageString = stagesToMatch.joinToString(" or ") { it.display() }
        return when {
            (stage in stagesToMatch || stage.category in stagesToMatch) -> pass(
                "Patient tumor stage is requested stage $stageString",
                "Adequate tumor stage"
            )

            (stagesToMatch.any { it.category == stage }) -> undetermined(
                "Undetermined if patient tumor stage $stage meets specific stage requirement(s) ($stageString)",
                "Undetermined if patient tumor stage $stage meets stage requirement(s) ($stageString)"
            )

            else -> fail("Patient tumor stage is not requested stage $stageString", "Inadequate tumor stage")
        }
    }

    private fun adjustStagesToMatch(stagesToMatch: Set<TumorStage>): Set<TumorStage> {
        return when (stagesToMatch.sorted()) {
            TumorStage.entries.filter { it.category == TumorStage.I } -> stagesToMatch + TumorStage.I
            TumorStage.entries.filter { it.category == TumorStage.II } -> stagesToMatch + TumorStage.II
            TumorStage.entries.filter { it.category == TumorStage.III } -> stagesToMatch + TumorStage.III
            TumorStage.entries.filter { it.category == TumorStage.IV } -> stagesToMatch + TumorStage.IV
            else -> stagesToMatch
        }
    }
}