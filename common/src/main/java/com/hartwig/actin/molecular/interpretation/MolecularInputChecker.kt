package com.hartwig.actin.molecular.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.filter.GeneFilterFactory.createAlwaysValid

class MolecularInputChecker(private val geneFilter: GeneFilter) {
    fun isGene(string: String): Boolean {
        return geneFilter.include(string)
    }

    companion object {
        private const val TERMINATION_CODON = "Ter"
        private val VALID_PROTEIN_ENDINGS: Set<String> = Sets.newHashSet("del", "dup", "ins", "=", "*", "fs", "ext*?")
        fun createAnyGeneValid(): MolecularInputChecker {
            return MolecularInputChecker(createAlwaysValid())
        }

        @JvmStatic
        fun isHlaAllele(string: String): Boolean {
            val asterixIndex = string.indexOf("*")
            val semicolonIndex = string.indexOf(":")
            return asterixIndex == 1 && semicolonIndex > asterixIndex
        }

        @JvmStatic
        fun isProteinImpact(string: String): Boolean {
            if (string == "?") {
                return true
            }
            if (string.length < 3) {
                return false
            }
            val first = string[0]
            val hasValidStart = Character.isUpperCase(first) || string.startsWith(TERMINATION_CODON)
            val last = string[string.length - 1]
            val hasValidEnd = hasSpecificValidProteinEnding(string) || Character.isUpperCase(last)
            val mid = string.substring(1, string.length - 1)
            val hasValidMid = hasSpecificValidProteinEnding(string) || mid.contains("_") || isPositiveNumber(mid)
            return hasValidStart && hasValidEnd && hasValidMid
        }

        private fun hasSpecificValidProteinEnding(string: String): Boolean {
            for (validProteinEnding in VALID_PROTEIN_ENDINGS) {
                if (string.endsWith(validProteinEnding)) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun isCodon(string: String): Boolean {
            if (string.length < 2) {
                return false
            }
            val first = string[0]
            val codon = string.substring(1)
            return Character.isUpperCase(first) && isPositiveNumber(codon)
        }

        private fun isPositiveNumber(codon: String): Boolean {
            return try {
                codon.toInt() > 0
            } catch (exception: NumberFormatException) {
                false
            }
        }
    }
}
