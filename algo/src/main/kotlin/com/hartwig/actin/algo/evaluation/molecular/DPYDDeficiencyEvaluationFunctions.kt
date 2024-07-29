package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry

object DPYDDeficiencyEvaluationFunctions {

    const val DPYD_GENE = "DPYD"
    private val expectedHaplotypeFunctions = setOf("normal function", "reduced function", "no function")

    fun isHomozygousDeficient(pharmaco: PharmacoEntry): Boolean {
        return pharmaco.haplotypes.none { it.function.lowercase() == "normal function" }
    }

    fun isProficient(pharmaco: PharmacoEntry): Boolean {
        return pharmaco.haplotypes.all { it.function.lowercase() == "normal function" }
    }

    fun containsUnexpectedHaplotypeFunction(pharmaco: PharmacoEntry): Boolean {
        return pharmaco.haplotypes.any { it.function.lowercase() !in expectedHaplotypeFunctions }
    }
}