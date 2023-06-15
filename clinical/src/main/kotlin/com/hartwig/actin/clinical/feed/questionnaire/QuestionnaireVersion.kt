package com.hartwig.actin.clinical.feed.questionnaire

import com.google.common.collect.Sets

internal enum class QuestionnaireVersion(private val specificSearchString: String, private val disallowedSearchStrings: Set<String>) {
    V1_6("ACTIN Questionnaire V1.6", Sets.newHashSet<String>()), V1_5(
        "ACTIN Questionnaire V1.5",
        Sets.newHashSet<String>()
    ),
    V1_4("ACTIN Questionnaire V1.4", Sets.newHashSet<String>()), V1_3(
        "CNS lesions:",
        Sets.newHashSet("ACTIN Questionnaire V1.4", "ACTIN Questionnaire V1.5", "ACTIN Questionnaire V1.6")
    ),
    V1_2("-Active:", Sets.newHashSet("CNS lesions:")), V1_1("- Active:", Sets.newHashSet<String>()), V1_0(
        "\\li0\\ri0",
        Sets.newHashSet<String>()
    ),
    V0_2("Other (e.g. pleural effusion)", Sets.newHashSet<String>()), V0_1(
        "Other (e.g. Osteoporosis, Pleural effusion)",
        Sets.newHashSet<String>()
    );

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
            val matches: MutableSet<QuestionnaireVersion> = Sets.newHashSet()
            for (version in values()) {
                if (version.isMatch(entry)) {
                    matches.add(version)
                }
            }
            check(matches.size <= 1) { "Questionnaire for " + entry.subject + " matched to multiple versions: " + matches }
            check(matches.isNotEmpty()) { "Could not find a match for questionnaire version for " + entry.subject }
            return matches.iterator().next()
        }
    }
}