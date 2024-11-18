package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isProficient
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoGene
import java.time.LocalDate

class HasHeterozygousDPYDDeficiency(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge, true) {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco.firstOrNull { it.gene == PharmacoGene.DPYD }
            ?: return EvaluationFactory.undetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")

        return when {
            !isHomozygousDeficient(pharmaco) && !isProficient(pharmaco) -> {
                EvaluationFactory.pass("Patient is heterozygous DPYD deficient", inclusionEvents = setOf("DPYD heterozygous deficient"))
            }

            else -> {
                EvaluationFactory.fail("Patient is not heterozygous DPYD deficient")
            }
        }
    }
}