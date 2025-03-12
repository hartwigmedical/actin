package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import java.time.LocalDate

class HasSufficientTumorMutationalBurden(private val minTumorMutationalBurden: Double, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val tumorMutationalBurden = test.characteristics.tumorMutationalBurden
            ?: return EvaluationFactory.undetermined(
                "Undetermined if TMB is above $minTumorMutationalBurden (no TMB result)",
                isMissingMolecularResultForEvaluation = true
            )

        if (tumorMutationalBurden >= minTumorMutationalBurden) {
            return EvaluationFactory.pass(
                "TMB is above $minTumorMutationalBurden",
                inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN)
            )
        }
        val tumorMutationalBurdenIsAlmostAllowed = minTumorMutationalBurden - tumorMutationalBurden <= 0.5
        return if (tumorMutationalBurdenIsAlmostAllowed && test.hasSufficientQualityButLowPurity()) {
            EvaluationFactory.warn(
                "TMB $tumorMutationalBurden almost exceeds min TMB $minTumorMutationalBurden"
                        + " while purity is low - perhaps a few mutations are missed",
                inclusionEvents = setOf(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN)
            )
        } else EvaluationFactory.fail("TMB $tumorMutationalBurden is not above $minTumorMutationalBurden")
    }
}