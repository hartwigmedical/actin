package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import java.time.LocalDate

class HasTumorMutationalLoadWithinRange(
    private val minTumorMutationalLoad: Int,
    private val maxTumorMutationalLoad: Int?,
    maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val tumorMutationalLoad = test.characteristics.tumorMutationalLoad
            ?: return EvaluationFactory.undetermined("TML undetermined")

        val meetsMinTumorLoad = tumorMutationalLoad >= minTumorMutationalLoad
        val meetsMaxTumorLoad = maxTumorMutationalLoad == null || tumorMutationalLoad <= maxTumorMutationalLoad
        if (meetsMinTumorLoad && meetsMaxTumorLoad) {
            return if (maxTumorMutationalLoad == null) {
                EvaluationFactory.pass(
                    "TML is sufficient (above $minTumorMutationalLoad)",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD)
                )
            } else {
                EvaluationFactory.pass(
                    "TML is sufficient (between $minTumorMutationalLoad - $maxTumorMutationalLoad)",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.ADEQUATE_TUMOR_MUTATIONAL_LOAD)
                )
            }
        }
        val tumorMutationalLoadIsAlmostAllowed = minTumorMutationalLoad - tumorMutationalLoad <= 5
        return if (tumorMutationalLoadIsAlmostAllowed && test.hasSufficientQualityButLowPurity()) {
            EvaluationFactory.warn(
                "TML $tumorMutationalLoad almost sufficient although purity is low",
                inclusionEvents = setOf(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD)
            )
        } else EvaluationFactory.fail("TML $tumorMutationalLoad is not sufficient")
    }
}