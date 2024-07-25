package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry

private const val DPYD_GENE = "DPYD"

private val expectedHaplotypeFunctions = setOf("normal function", "reduced function", "no function")

class HasHeterozygousDPYDDeficiency internal constructor() : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco.filter { it.gene == DPYD_GENE }

        if (pharmaco.isEmpty()) {
            return EvaluationFactory.recoverableUndetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")
        }

        return when {
            containsUnexpectedHaplotypeFunction(pharmaco) -> {
                EvaluationFactory.recoverableUndetermined(
                    "DPYD haplotype function cannot be determined due to unexpected haplotype function",
                    "DPYD haplotype function undetermined"
                )
            }

            isHeterozygousDeficient(pharmaco) -> {
                EvaluationFactory.pass("Patient is heterozygous DPYD deficient", inclusionEvents = setOf("DPYD deficient"))
            }

            else -> {
                EvaluationFactory.fail("Patient is not heterozygous DPYD deficient")
            }
        }
    }

    private fun containsUnexpectedHaplotypeFunction(pharmaco: List<PharmacoEntry>): Boolean {
        return pharmaco.any { pharmacoEntry ->
            pharmacoEntry.haplotypes.any { it.function.lowercase() !in expectedHaplotypeFunctions }
        }
    }

    private fun isHeterozygousDeficient(pharmaco: List<PharmacoEntry>): Boolean {
        return pharmaco.any { pharmacoEntry ->
            pharmacoEntry.haplotypes.any { it.function.lowercase() != "normal function" && it.alleleCount == 1 }
        }
    }
}