package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry

object DPYDDeficiencyEvaluationFunctions {

    const val DPYD_GENE = "DPYD"
    private val expectedHaplotypeFunctions = setOf("normal function", "reduced function", "no function")

    fun isHomozygousDeficient(pharmaco: List<PharmacoEntry>): Boolean {
        return pharmaco.none { pharmacoEntry ->
            pharmacoEntry.haplotypes.any { it.function.lowercase() == "normal function" }
        }
    }

    fun isProficient(pharmaco: List<PharmacoEntry>): Boolean {
        return pharmaco.all { pharmacoEntry -> pharmacoEntry.haplotypes.all { it.function.lowercase() == "normal function" } }
    }

    fun containsUnexpectedHaplotypeFunction(pharmaco: List<PharmacoEntry>): Boolean {
        return pharmaco.any { pharmacoEntry ->
            pharmacoEntry.haplotypes.any { it.function.lowercase() !in expectedHaplotypeFunctions }
        }
    }
}