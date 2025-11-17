package com.hartwig.actin.report.pdf.tables.trial

object TrialFormatFunctions {

    fun generateCohortsFromTrialsString(cohortCount: Int, trialCount: Int): String {
        val formatTrialCount = formatCountWithLabel(trialCount, "trial")
        return when {
            trialCount > cohortCount -> throw IllegalStateException("Trial count is > 0 while cohort count is 0")
            cohortCount > 0 && cohortCount == trialCount -> "($formatTrialCount)"
            cohortCount > 0 -> "(${formatCountWithLabel(cohortCount, "cohort")} from $formatTrialCount)"
            else -> "(0 trials)"
        }
    }

    fun formatCountWithLabel(count: Int, word: String): String {
        return "$count $word${if (count > 1) "s" else ""}"
    }
}