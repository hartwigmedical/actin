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
            ?: return EvaluationFactory.undetermined(
                "Undetermined if TML is sufficient (no TML result)",
                isMissingMolecularResultForEvaluation = true
            )

        val meetsMinTumorLoad = tumorMutationalLoad >= minTumorMutationalLoad
        val meetsMaxTumorLoad = maxTumorMutationalLoad == null || tumorMutationalLoad <= maxTumorMutationalLoad
        val message = if (maxTumorMutationalLoad == null) {
            "above $minTumorMutationalLoad"
        } else {
            "between $minTumorMutationalLoad and $maxTumorMutationalLoad"
        }

        if (meetsMinTumorLoad && meetsMaxTumorLoad) {
            return if (maxTumorMutationalLoad == null) {
                EvaluationFactory.pass(
                    "TML is $message",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD)
                )
            } else {
                EvaluationFactory.pass(
                    "TML is $message",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.ADEQUATE_TUMOR_MUTATIONAL_LOAD)
                )
            }
        }
        val tumorMutationalLoadIsAlmostAllowed = minTumorMutationalLoad - tumorMutationalLoad <= 5
        return if (tumorMutationalLoadIsAlmostAllowed && test.hasSufficientQualityButLowPurity()) {
            EvaluationFactory.warn(
                "TML $tumorMutationalLoad almost $message" +
                        " while purity is low - perhaps a few mutations are missed",
                inclusionEvents = setOf(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD)
            )
        } else EvaluationFactory.fail("TML $tumorMutationalLoad is not $message")
    }
}