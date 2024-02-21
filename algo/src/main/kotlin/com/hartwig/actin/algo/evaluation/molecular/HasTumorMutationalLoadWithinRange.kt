package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class HasTumorMutationalLoadWithinRange(
    private val minTumorMutationalLoad: Int, private val maxTumorMutationalLoad: Int?
) : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val tumorMutationalLoad = molecular.characteristics.tumorMutationalLoad
            ?: return EvaluationFactory.fail("Unknown tumor mutational load (TML)", "TML unknown")

        val meetsMinTumorLoad = tumorMutationalLoad >= minTumorMutationalLoad
        val meetsMaxTumorLoad = maxTumorMutationalLoad == null || tumorMutationalLoad <= maxTumorMutationalLoad
        if (meetsMinTumorLoad && meetsMaxTumorLoad) {
            return if (maxTumorMutationalLoad == null) {
                EvaluationFactory.pass(
                    "Tumor mutational load (TML) of sample $tumorMutationalLoad is higher than requested minimal TML of $minTumorMutationalLoad",
                    "Adequate TML",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD)
                )
            } else {
                EvaluationFactory.pass(
                    "Tumor mutational load (TML) of sample $tumorMutationalLoad is between requested TML range of"
                            + " $minTumorMutationalLoad - $maxTumorMutationalLoad",
                    "Adequate TML",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.ADEQUATE_TUMOR_MUTATIONAL_LOAD)
                )
            }
        }
        val tumorMutationalLoadIsAlmostAllowed = minTumorMutationalLoad - tumorMutationalLoad <= 5
        return if (tumorMutationalLoadIsAlmostAllowed && molecular.hasSufficientQuality
            && !molecular.hasSufficientQualityAndPurity
        ) {
            EvaluationFactory.warn(
                "Tumor mutational load (TML) of sample $tumorMutationalLoad almost exceeds $minTumorMutationalLoad"
                        + " while purity is low: perhaps a few mutations are missed and TML is adequate",
                "TML almost sufficient while purity is low",
                inclusionEvents = setOf(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD)
            )
        } else EvaluationFactory.fail(
            "Tumor mutational load (TML) of sample $tumorMutationalLoad is not within specified range", "Inadequate TML"
        )
    }
}