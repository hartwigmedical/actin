package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasTumorStage(private val stagesToMatch: Set<TumorStage>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined("Exact tumor stage undetermined (tumor stage missing)")
        val stageMessage =
            record.tumor.stage?.display() ?: "(derived stage: ${record.tumor.derivedStages?.joinToString(" or ") { it.display() }})"
        val allStagesToMatch = stagesToMatch + additionalStagesToMatch(stagesToMatch)
        val stagesToMatchMessage = stagesToMatch.joinToString(" or ") { it.display() }

        return when {
            stage in allStagesToMatch || stage.category in allStagesToMatch -> {
                EvaluationFactory.pass("Tumor stage $stageMessage meets requested stage(s) $stagesToMatchMessage")
            }

            allStagesToMatch.any { it.category == stage } -> {
                EvaluationFactory.undetermined("Undetermined if tumor stage $stageMessage meets requested stage(s) $stagesToMatchMessage")
            }

            else -> {
                EvaluationFactory.fail("Tumor stage $stageMessage does not meet requested stage(s) $stagesToMatchMessage")
            }
        }
    }

    private fun additionalStagesToMatch(stagesToMatch: Set<TumorStage>): List<TumorStage> {
        return TumorStage.entries.groupBy(TumorStage::category)
            .filter { (_, stagesInCategory) -> stagesInCategory.all(stagesToMatch::contains) }
            .keys.filterNotNull()
    }
}