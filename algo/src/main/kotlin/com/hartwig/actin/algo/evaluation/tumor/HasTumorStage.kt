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
        val stageMessage = stagesToMatch.sorted().joinToString(" or ") { it.display() }
        val adjustedStagesToMatch = adjustStagesToMatch(stagesToMatch)
        val stage = record.tumor.stage

        if (stage == null) {
            val derivedStages = record.tumor.derivedStages
            val derivedStageMessage = "derived ${derivedStages?.joinToString(" or ") { it.display() }} based on lesions"

            return when {
                (derivedStages?.size == 1) -> evaluateWithStage(derivedStages.iterator().next(), adjustedStagesToMatch, stageMessage)

                (derivedStages?.map { evaluateWithStage(it, adjustedStagesToMatch, stageMessage) }
                    ?.all { it.result == EvaluationResult.PASS } == true) -> pass(
                    "No tumor stage details present but based on lesions requested stage $stageMessage met - $derivedStageMessage",
                    "Tumor stage data missing but requested stage $stageMessage met - $derivedStageMessage"
                )

                (derivedStages?.map { evaluateWithStage(it, adjustedStagesToMatch, stageMessage) }
                    ?.any { it.result in listOf(EvaluationResult.PASS, EvaluationResult.UNDETERMINED) } == true) ->
                    undetermined(
                        "Unknown if tumor stage is $stageMessage (no tumor stage details provided) - $derivedStageMessage",
                        "Unknown if tumor stage is $stageMessage (data missing) - $derivedStageMessage"
                    )

                else ->
                    fail(
                        "No tumor stage details present but based on lesions requested stage $stageMessage not met - $derivedStageMessage",
                        "Tumor stage data missing but requested stage $stageMessage not met - $derivedStageMessage"
                    )
            }
        }
        return evaluateWithStage(stage, adjustedStagesToMatch, stageMessage)
    }

    private fun evaluateWithStage(
        stage: TumorStage,
        stagesToMatch: Set<TumorStage>,
        stageMessage: String
    ): Evaluation {
        return when {
            (stage in stagesToMatch || stage.category in stagesToMatch) -> pass(
                "Patient tumor stage $stage meets requested stage(s) $stageMessage",
                "Patient tumor stage $stage meets requested stage(s) $stageMessage"
            )

            (stagesToMatch.any { it.category == stage }) -> undetermined(
                "Undetermined if patient tumor stage $stage meets specific stage requirement(s) ($stageMessage)",
                "Undetermined if patient tumor stage $stage meets stage requirement(s) ($stageMessage)"
            )

            else -> fail(
                "Patient tumor stage $stage does not meet requested stage(s) $stageMessage",
                "Patient tumor stage $stage does not meet requested stage(s) $stageMessage"
            )
        }
    }

    private fun adjustStagesToMatch(stagesToMatch: Set<TumorStage>): Set<TumorStage> {
        return TumorStage.entries.filter { stage ->
            stage in listOf(TumorStage.I, TumorStage.II, TumorStage.III, TumorStage.IV) &&
                    stagesToMatch.filter { it.category == stage }.sorted() == TumorStage.entries.filter { it.category == stage }
        }.toSet() + stagesToMatch
    }
}