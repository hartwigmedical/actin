package com.hartwig.actin.clinical.feed.questionnaire

internal enum class QuestionnaireVersion(private val specificSearchString: String, private val disallowedSearchStrings: Set<String>) {
    V1_7("ACTIN Questionnaire V1.7", emptySet()),
    V1_6("ACTIN Questionnaire V1.6", emptySet()),
    V1_5("ACTIN Questionnaire V1.5", emptySet()),
    V1_4("ACTIN Questionnaire V1.4", emptySet()),
    V1_3("CNS lesions:", setOf("ACTIN Questionnaire V1.4", "ACTIN Questionnaire V1.5", "ACTIN Questionnaire V1.6")),
    V1_2("-Active:", setOf("CNS lesions:")),
    V1_1("- Active:", emptySet()),
    V1_0("\\li0\\ri0", emptySet()),
    V0_2("Other (e.g. pleural effusion)", emptySet()),
    V0_1("Other (e.g. Osteoporosis, Pleural effusion)", emptySet());

    private fun isMatch(entry: QuestionnaireEntry): Boolean {
        val lines = entry.text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var match = false
        for (line in lines) {
            if (line.contains(specificSearchString)) {
                match = true
            }
            for (disallowedSearchString in disallowedSearchStrings) {
                if (line.contains(disallowedSearchString)) {
                    return false
                }
            }
        }
        return match
    }

    companion object {
        fun version(entry: QuestionnaireEntry): QuestionnaireVersion {
            val matches: Set<QuestionnaireVersion> = values().filter { it.isMatch(entry) }.toSet()

            check(matches.size <= 1) { "Questionnaire for " + entry.subject + " matched to multiple versions: " + matches }
            check(matches.isNotEmpty()) { "Could not find a match for questionnaire version for " + entry.subject }
            return matches.iterator().next()
        }
    }
}