package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry

class HasHomozygousDPYDDeficiency internal constructor() : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco

        if (pharmaco.none { it.gene == "DPYD" }) {
            return EvaluationFactory.recoverableUndetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")
        }

        val isHomozygousDeficient = isHomozygousDeficient(pharmaco)

        return if (isHomozygousDeficient) {
            EvaluationFactory.pass("Patient is homozygous DPYD deficient", inclusionEvents = setOf("DPYD deficient"))
        } else
            EvaluationFactory.fail("Patient is not homozygous DPYD deficient")
    }

    private fun isHomozygousDeficient(pharmaco: Set<PharmacoEntry>): Boolean {
        for (pharmacoEntry in pharmaco) {
            if (pharmacoEntry.gene == "DPYD" && pharmacoEntry.haplotypes.any { it.function == "Normal function" }) {
                return false
            }
        }
        return true
    }
}


