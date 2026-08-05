package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents

class HasTumorMutationalLoadWithinRange(
    private val minTumorMutationalLoad: Int,
    private val maxTumorMutationalLoad: Int?,
    labels: EvaluationLabels.Molecular
) : MolecularEvaluationFunction(labels = labels) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val tumorMutationalLoad = test.characteristics.tumorMutationalLoad?.score
            ?: return EvaluationFactory.undetermined(
                labels.hasTumorMutationalLoadWithinRangeUndetermined(),
                isMissingMolecularResultForEvaluation = true
            )

        val meetsMinTumorLoad = tumorMutationalLoad >= minTumorMutationalLoad
        val meetsMaxTumorLoad = maxTumorMutationalLoad == null || tumorMutationalLoad <= maxTumorMutationalLoad
        val message = if (maxTumorMutationalLoad == null) {
            labels.hasTumorMutationalLoadWithinRangeMessageAbove(minTumorMutationalLoad)
        } else {
            labels.hasTumorMutationalLoadWithinRangeMessageBetween(minTumorMutationalLoad, maxTumorMutationalLoad)
        }

        if (meetsMinTumorLoad && meetsMaxTumorLoad) {
            return if (maxTumorMutationalLoad == null) {
                EvaluationFactory.pass(
                    labels.hasTumorMutationalLoadWithinRangePass(message),
                    inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD)
                )
            } else {
                EvaluationFactory.pass(
                    labels.hasTumorMutationalLoadWithinRangePass(message),
                    inclusionEvents = setOf(MolecularCharacteristicEvents.ADEQUATE_TUMOR_MUTATIONAL_LOAD)
                )
            }
        }
        val tumorMutationalLoadIsAlmostAllowed = minTumorMutationalLoad - tumorMutationalLoad <= 5
        return if (tumorMutationalLoadIsAlmostAllowed && test.hasSufficientQualityButLowPurity()) {
            EvaluationFactory.warn(
                labels.hasTumorMutationalLoadWithinRangeWarn(tumorMutationalLoad, message),
                inclusionEvents = setOf(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD)
            )
        } else EvaluationFactory.fail(labels.hasTumorMutationalLoadWithinRangeFail(tumorMutationalLoad, message))
    }
}