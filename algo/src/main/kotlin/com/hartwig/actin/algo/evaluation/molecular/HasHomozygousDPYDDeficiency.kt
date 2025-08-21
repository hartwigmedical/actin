package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import java.time.LocalDate

class HasHomozygousDPYDDeficiency(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge, true) {

    override fun noMolecularTestEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            "No molecular data to determine homozygous DPYD deficiency",
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val pharmaco = test.pharmaco.firstOrNull { it.gene == PharmacoGene.DPYD }
            ?: return EvaluationFactory.undetermined("DPYD haplotype undetermined", isMissingMolecularResultForEvaluation = true)

        return when {
            isHomozygousDeficient(pharmaco) -> {
                EvaluationFactory.pass(
                    "Homozygous DPYD deficiency detected",
                    inclusionEvents = setOf("DPYD homozygous deficient")
                )
            }

            else -> {
                EvaluationFactory.fail("Is not homozygous DPYD deficient")
            }
        }
    }
}