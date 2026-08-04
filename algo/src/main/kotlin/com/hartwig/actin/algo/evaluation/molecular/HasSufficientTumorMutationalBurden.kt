package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents

class HasSufficientTumorMutationalBurden(private val minTumorMutationalBurden: Double, labels: EvaluationLabels.Molecular) :
    MolecularEvaluationFunction(labels = labels) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val tumorMutationalBurden = test.characteristics.tumorMutationalBurden?.score
            ?: return EvaluationFactory.undetermined(
                labels.hasSufficientTumorMutationalBurdenUndetermined(minTumorMutationalBurden),
                isMissingMolecularResultForEvaluation = true
            )

        if (tumorMutationalBurden >= minTumorMutationalBurden) {
            return EvaluationFactory.pass(
                labels.hasSufficientTumorMutationalBurdenPass(minTumorMutationalBurden),
                inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN)
            )
        }
        val tumorMutationalBurdenIsAlmostAllowed = minTumorMutationalBurden - tumorMutationalBurden <= 0.5
        return if (tumorMutationalBurdenIsAlmostAllowed && test.hasSufficientQualityButLowPurity()) {
            EvaluationFactory.warn(
                labels.hasSufficientTumorMutationalBurdenWarn(tumorMutationalBurden, minTumorMutationalBurden),
                inclusionEvents = setOf(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN)
            )
        } else EvaluationFactory.fail(labels.hasSufficientTumorMutationalBurdenFail(tumorMutationalBurden, minTumorMutationalBurden))
    }
}