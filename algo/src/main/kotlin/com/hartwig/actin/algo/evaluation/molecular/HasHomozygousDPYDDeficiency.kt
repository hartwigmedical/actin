package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry

class HasHomozygousDPYDDeficiency internal constructor() : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco
        val geneDPYD = "DPYD"

        if (pharmaco.none { it.gene == geneDPYD }) {
            return EvaluationFactory.recoverableUndetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")
        }

        val containsUnexpectedHaplotypeFunction = containsUnexpectedHaplotypeFunction(pharmaco, geneDPYD)
        val isHomozygousDeficient = isHomozygousDeficient(pharmaco, geneDPYD)

        return when {
            (containsUnexpectedHaplotypeFunction) -> {
                EvaluationFactory.recoverableUndetermined(
                    "DPYD haplotype function cannot be determined due to unexpected haplotype function",
                    "DPYD haplotype function undetermined"
                )
            }

            (isHomozygousDeficient) -> {
                EvaluationFactory.pass("Patient is homozygous DPYD deficient", inclusionEvents = setOf("DPYD deficient"))
            }

            else -> {
                EvaluationFactory.fail("Patient is not homozygous DPYD deficient")
            }
        }
    }

    private fun containsUnexpectedHaplotypeFunction(pharmaco: Set<PharmacoEntry>, gene: String): Boolean {
        for (pharmacoEntry in pharmaco) {
            if (pharmacoEntry.gene == gene && pharmacoEntry.haplotypes.any { it.function.lowercase() !in expectedHaplotypeFunctions}) {
                return true
            }
        }
        return false
    }


    private fun isHomozygousDeficient(pharmaco: Set<PharmacoEntry>, gene: String): Boolean {
        for (pharmacoEntry in pharmaco) {
            if (pharmacoEntry.gene == gene && pharmacoEntry.haplotypes.any { it.function.lowercase() == "normal function" }) {
                return false
            }
        }
        return true
    }

    private val expectedHaplotypeFunctions = setOf("normal function", "reduced function", "no function")
}


