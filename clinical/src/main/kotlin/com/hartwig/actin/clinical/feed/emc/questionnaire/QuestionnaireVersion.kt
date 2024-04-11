package com.hartwig.actin.clinical.feed.emc.questionnaire

internal enum class QuestionnaireVersion(private val specificSearchString: String, private val disallowedRegex: Regex?) {
    V0_1("Other (e.g. Osteoporosis, Pleural effusion)", null),
    V0_2("Other (e.g. pleural effusion)", null),
    V1_0("\\li0\\ri0", null),
    V1_1("- Active:", null),
    V1_2("-Active:", Regex("CNS lesions:")),
    V1_3("CNS lesions:", Regex("ACTIN Questionnaire V1.[^0123]")),
    V1_4("ACTIN Questionnaire V1.4", null),
    V1_5("ACTIN Questionnaire V1.5", null),
    V1_6("ACTIN Questionnaire V1.6", null),
    V1_7("ACTIN Questionnaire V1.7", null);

    private fun isMatch(entry: QuestionnaireEntry): Boolean {
        val lines = entry.text.split("\n").dropLastWhile { it.isEmpty() }
        return lines.any { it.contains(specificSearchString) }
                && (disallowedRegex == null || lines.none { disallowedRegex.containsMatchIn(it) })
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