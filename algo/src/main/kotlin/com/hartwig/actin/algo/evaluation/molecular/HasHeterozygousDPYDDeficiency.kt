package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isProficient
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import java.time.LocalDate

class HasHeterozygousDPYDDeficiency(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge, true) {

    override fun noMolecularRecordEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            "No molecular data to determine heterozygous DPYD deficiency",
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco.firstOrNull { it.gene == PharmacoGene.DPYD }
            ?: return EvaluationFactory.undetermined("DPYD haplotype undetermined")

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