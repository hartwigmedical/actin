package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isSpecificStageMatch
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasTumorStage(private val stagesToMatch: Set<TumorStage>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined("Exact tumor stage undetermined (tumor stage missing)")
        val stageMessage = stage.display()
        val allStagesToMatch = stagesToMatch + additionalStagesToMatch(stagesToMatch)
        val stagesToMatchMessage = stagesToMatch.joinToString(" or ") { it.display() }

        return when {
            isSpecificStageMatch(stage, allStagesToMatch).first -> {
                EvaluationFactory.pass("Tumor stage $stageMessage meets requested stage(s) $stagesToMatchMessage")
            }

            isSpecificStageMatch(stage, allStagesToMatch).second -> {
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