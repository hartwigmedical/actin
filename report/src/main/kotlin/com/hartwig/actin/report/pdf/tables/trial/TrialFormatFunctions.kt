package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.pdf.ReportLabels

object TrialFormatFunctions {

    fun generateCohortsFromTrialsString(cohortCount: Int, trialCount: Int, labels: ReportLabels): String {
        val formatTrialCount = formatCountWithLabel(trialCount, labels.misc.trial())
        return when {
            trialCount > cohortCount -> throw IllegalStateException("Trial count > cohort count - which should not be possible")
            cohortCount > 0 && cohortCount == trialCount -> "($formatTrialCount)"
            cohortCount > 0 -> "(${formatCountWithLabel(cohortCount, labels.misc.cohort())} ${labels.misc.from()} $formatTrialCount)"
            else -> labels.misc.zeroTrials()
        }
    }

    fun formatCountWithLabel(count: Int, word: String): String {
        return "$count $word${if (count > 1) "s" else ""}"
    }
}
