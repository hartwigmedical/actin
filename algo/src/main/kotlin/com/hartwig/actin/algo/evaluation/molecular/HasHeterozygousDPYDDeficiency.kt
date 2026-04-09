package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isProficient
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene

class HasHeterozygousDPYDDeficiency: MolecularEvaluationFunction(true) {

    override fun noMolecularTestEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            "No molecular data to determine heterozygous DPYD deficiency",
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val pharmaco = test.pharmaco.firstOrNull { it.gene == PharmacoGene.DPYD }
            ?: return EvaluationFactory.undetermined(
                "DPYD haplotype undetermined",
                isMissingMolecularResultForEvaluation = true
            )

        return when {
            !isHomozygousDeficient(pharmaco) && !isProficient(pharmaco) -> {
                EvaluationFactory.pass(
                    "Heterozygous DPYD deficiency detected",
                    inclusionEvents = setOf("DPYD heterozygous deficient")
                )
            }

            else -> {
                EvaluationFactory.fail("Is not heterozygous DPYD deficient")
            }
        }
    }
}