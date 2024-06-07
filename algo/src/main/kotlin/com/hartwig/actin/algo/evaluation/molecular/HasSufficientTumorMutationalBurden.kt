package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class HasSufficientTumorMutationalBurden(private val minTumorMutationalBurden: Double) : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val tumorMutationalBurden = molecular.characteristics.tumorMutationalBurden
            ?: return EvaluationFactory.fail("Unknown tumor mutational burden (TMB)", "Unknown TMB")

        if (tumorMutationalBurden >= minTumorMutationalBurden) {
            return EvaluationFactory.pass(
                "Tumor mutational burden (TMB) of sample $tumorMutationalBurden is sufficient",
                "Adequate TMB",
                inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN)
            )
        }
        val tumorMutationalBurdenIsAlmostAllowed = minTumorMutationalBurden - tumorMutationalBurden <= 0.5
        return if (tumorMutationalBurdenIsAlmostAllowed && molecular.hasSufficientQualityButLowPurity()
        ) {
            EvaluationFactory.warn(
                "Tumor mutational burden (TMB) of sample $tumorMutationalBurden almost exceeds $minTumorMutationalBurden"
                        + " while purity is low: perhaps a few mutations are missed and TMB is adequate",
                "TMB almost sufficient while purity is low",
                inclusionEvents = setOf(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN)
            )
        } else EvaluationFactory.fail(
            "Tumor mutational burden (TMB) of sample $tumorMutationalBurden is not within specified range", "Inadequate TMB"
        )
    }
}