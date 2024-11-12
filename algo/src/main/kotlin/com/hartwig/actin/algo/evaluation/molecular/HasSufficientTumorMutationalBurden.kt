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
            ?: return EvaluationFactory.undetermined("Unknown tumor mutational burden (TMB)", "Unknown TMB")

        if (tumorMutationalBurden >= minTumorMutationalBurden) {
            return EvaluationFactory.pass(
                "Tumor mutational burden (TMB) of sample $tumorMutationalBurden is sufficient",
                "Adequate TMB",
                inclusionEvents = setOf(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN)
            )
        }
        val tumorMutationalBurdenIsAlmostAllowed = minTumorMutationalBurden - tumorMutationalBurden <= 0.5
        return if (tumorMutationalBurdenIsAlmostAllowed && test.hasSufficientQualityButLowPurity()) {
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