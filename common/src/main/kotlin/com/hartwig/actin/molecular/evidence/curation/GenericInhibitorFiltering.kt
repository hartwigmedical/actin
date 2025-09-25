package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.serve.datamodel.efficacy.Treatment

object GenericInhibitorFiltering {

    private val INHIBITOR_KEYWORDS = setOf("inhibitor", "inhibitors")
    private val GENERIC_LAST_TOKENS = setOf("beta", "receptor")
    private val GENERIC_TWO_TOKEN_PREFIXES = setOf("gamma secretase", "immune checkpoint")
    private val TOKEN_SEPARATOR_REGEX = "[()\\-/]".toRegex()
    private val TOKEN_SPLIT_REGEX = "\\s+".toRegex()

    fun isGenericInhibitor(treatment: Treatment): Boolean {
        return treatment.treatmentApproachesDrugClass()
            .asSequence()
            .filter { it.isNotBlank() }
            .any { drugClass -> isGenericInhibitorClass(drugClass) }
    }

    private fun isGenericInhibitorClass(drugClass: String): Boolean {
        val normalized = drugClass.lowercase()
        if (INHIBITOR_KEYWORDS.none { normalized.contains(it) }) {
            return false
        }

        val tokens = extractTokens(normalized)
        val inhibitorIndex = tokens.indexOfFirst { it in INHIBITOR_KEYWORDS }
        if (inhibitorIndex <= 0) {
            return false
        }

        val prefixTokens = tokens.subList(0, inhibitorIndex)

        // Any pattern "token inhibitor(s)" counts as a generic inhibitor.
        if (prefixTokens.size == 1) {
            return true
        }

        // "token beta inhibitor" and "token receptor inhibitor" are generic.
        if (prefixTokens.last() in GENERIC_LAST_TOKENS) {
            return true
        }

        // Known multi-token prefixes such as "Gamma secretase inhibitor" and "Immune checkpoint inhibitor".
        val lastTwoTokens = prefixTokens.takeLast(2).joinToString(" ")
        if (lastTwoTokens in GENERIC_TWO_TOKEN_PREFIXES) {
            return true
        }

        // other cases are assumed to be specific, as in "KRAS G12C Inhibitor", without having to parse and validate hgvs syntax
        return false
    }

    private fun extractTokens(value: String): List<String> {
        return value
            .replace(TOKEN_SEPARATOR_REGEX, " ")
            .split(TOKEN_SPLIT_REGEX)
            .filter { it.isNotBlank() }
    }
}
