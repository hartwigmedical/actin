package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorStage

class HasTumorStage internal constructor(
    private val tumorStageDerivationFunction: TumorStageDerivationFunction, private val stageToMatch: TumorStage
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.clinical.tumor.stage
        if (stage == null) {
            val derivedStages = tumorStageDerivationFunction.apply(record.clinical.tumor)?.toSet()
            return if (derivedStages?.size == 1) {
                evaluateWithStage(derivedStages.iterator().next())
            } else if (derivedStages?.map { evaluateWithStage(it) }?.any { it.result == EvaluationResult.PASS } == true) {
                val derivedStageMessage = "assumed ${derivedStages.joinToString(" or ") { it.display() }} based on lesions"
                undetermined(
                    "No tumor stage details present but $derivedStageMessage",
                    "Missing tumor stage details - $derivedStageMessage"
                )
            } else {
                fail("No tumor stage details present but based on lesions requested stage cannot be met",
                    "Tumor stage unknown but requested stage not met based on lesions")
            }
        }
        return evaluateWithStage(stage)
    }

    private fun evaluateWithStage(stage: TumorStage): Evaluation {
        return if (stage == stageToMatch || stage.category == stageToMatch) {
            pass("Patient tumor stage is exact stage " + stageToMatch.display(), "Adequate tumor stage")
        } else {
            fail("Patient tumor stage is not exact stage " + stageToMatch.display(), "Inadequate tumor stage")
        }
    }
}