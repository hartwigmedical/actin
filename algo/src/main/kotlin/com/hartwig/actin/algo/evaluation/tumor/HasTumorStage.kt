package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasTumorStage(private val stagesToMatch: Set<TumorStage>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (stagesToMatch.isEmpty()) {
            throw IllegalStateException("No stages to match configured")
        }
        val stageMessage = Format.concatItemsWithOr(stagesToMatch)
        val allStagesToMatch = stagesToMatch + additionalStagesToMatch(stagesToMatch)
        val stage = record.tumor.stage

        if (stage == null) {
            val derivedStages = record.tumor.derivedStages
            val derivedStageMessage = "derived ${derivedStages?.let(Format::concatItemsWithOr)} based on lesions"

            return when {
                derivedStages == null -> {
                    undetermined(
                        "Tumor stage data missing and not possible to derive if requested stage(s) $stageMessage met"
                    )
                }

                derivedStages.size == 1 -> {
                    evaluateWithStage(derivedStages.iterator().next(), allStagesToMatch, stageMessage)
                }

                derivedStages.map { evaluateWithStage(it, allStagesToMatch, stageMessage) }.all { it.result == EvaluationResult.PASS } -> {
                    pass("Tumor stage data missing but stage $stageMessage met - $derivedStageMessage")
                }

                derivedStages.map { evaluateWithStage(it, allStagesToMatch, stageMessage) }
                    .any { it.result in listOf(EvaluationResult.PASS, EvaluationResult.UNDETERMINED) } -> {
                    undetermined("Unknown if tumor stage is $stageMessage (data missing) - $derivedStageMessage")
                }

                else -> {
                    fail("Tumor stage data missing but stage $stageMessage not met - $derivedStageMessage")
                }
            }
        }
        return evaluateWithStage(stage, allStagesToMatch, stageMessage)
    }

    private fun evaluateWithStage(
        stage: TumorStage,
        stagesToMatch: Set<TumorStage>,
        stageMessage: String
    ): Evaluation {
        return when {
            stage in stagesToMatch || stage.category in stagesToMatch -> {
                pass("Tumor stage is $stage")
            }

            stagesToMatch.any { it.category == stage } -> {
                undetermined("Undetermined if patient tumor stage $stage meets stage requirement(s) ($stageMessage)")
            }

            else -> {
                fail("Tumor stage $stage not equal to $stageMessage")
            }
        }
    }

    private fun additionalStagesToMatch(stagesToMatch: Set<TumorStage>): List<TumorStage> {
        return TumorStage.entries.groupBy(TumorStage::category)
            .filter { (_, stagesInCategory) -> stagesInCategory.all(stagesToMatch::contains) }
            .keys.filterNotNull()
    }
}