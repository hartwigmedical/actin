package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.filter.GeneFilterFactory.createAlwaysValid

class MolecularInputChecker(private val geneFilter: GeneFilter) {

    fun isGene(string: String): Boolean {
        return geneFilter.include(string)
    }

    companion object {
        private const val TERMINATION_CODON = "Ter"
        private val VALID_PROTEIN_ENDINGS: Set<String> = setOf("del", "dup", "ins", "=", "*", "fs", "ext*?")

        fun createAnyGeneValid(): MolecularInputChecker {
            return MolecularInputChecker(createAlwaysValid())
        }

        fun isHlaAllele(string: String): Boolean {
            val asteriskIndex = string.indexOf("*")
            val semicolonIndex = string.indexOf(":")
            return asteriskIndex == 1 && semicolonIndex > asteriskIndex
        }

        fun isHlaGroup(string: String): Boolean {
            return Regex("[A-Z]+\\*\\d{2}").matches(string)
        }

        fun isHaplotype(string: String): Boolean {
            val asterixIndex = string.indexOf("*")
            val semicolonIndex = string.indexOf("_")
            return asterixIndex == 0 && semicolonIndex > asterixIndex && string[1].isDigit()
        }

        fun isProteinImpact(string: String): Boolean {
            if (string == "?") {
                return true
            }
            if (string.length < 3 || string.endsWith('X')) {
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
            return VALID_PROTEIN_ENDINGS.any { string.endsWith(it) }
        }

        fun isCodon(string: String): Boolean {
            return string.length >= 2 && Character.isUpperCase(string.first()) && isPositiveNumber(string.substring(1))
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
