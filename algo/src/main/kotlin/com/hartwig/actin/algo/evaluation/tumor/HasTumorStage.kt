package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.TumorEvaluationFunctions.isSpecificStageMatch
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasTumorStage(private val stagesToMatch: Set<TumorStage>, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined(labels.hasTumorStageUndeterminedMissing())
        val stageMessage = stage.display()
        val allStagesToMatch = stagesToMatch + additionalStagesToMatch(stagesToMatch)
        val stagesToMatchMessage = stagesToMatch.joinToString(" or ") { it.display() }

        return when {
            isSpecificStageMatch(stage, allStagesToMatch).first -> {
                EvaluationFactory.pass(labels.hasTumorStagePass(stageMessage, stagesToMatchMessage))
            }

            isSpecificStageMatch(stage, allStagesToMatch).second -> {
                EvaluationFactory.undetermined(labels.hasTumorStageUndetermined(stageMessage, stagesToMatchMessage))
            }

            else -> {
                EvaluationFactory.fail(labels.hasTumorStageFail(stageMessage, stagesToMatchMessage))
            }
        }
    }

    private fun additionalStagesToMatch(stagesToMatch: Set<TumorStage>): List<TumorStage> {
        return TumorStage.entries.groupBy(TumorStage::category)
            .filter { (_, stagesInCategory) -> stagesInCategory.all(stagesToMatch::contains) }
            .keys.filterNotNull()
    }
}