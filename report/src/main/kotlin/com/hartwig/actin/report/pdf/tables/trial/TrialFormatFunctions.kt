package com.hartwig.actin.report.pdf.tables.trial

object TrialFormatFunctions {

    val FOOTNOTES = setOf(
        FOOT_NOTE_NO_CLINICAL_DATA_USED,
        FOOT_NOTE_CLINICAL_DATA_EXCLUDED,
        FOOT_NOTE_FILTERED_NATIONAL_EXTERNAL_TRIALS,
        FOOT_NOTE_SAME_MOLECULAR_TARGET
    )

    const val FOOT_NOTE_NO_CLINICAL_DATA_USED =
        "Trials matched solely on molecular event and tumor type (no clinical data used) are shown in italicized, smaller font."
    const val FOOT_NOTE_CLINICAL_DATA_EXCLUDED =
        "Trials in this table are matched solely on molecular event and tumor type (clinical data excluded)."
    const val FOOT_NOTE_FILTERED_NATIONAL_EXTERNAL_TRIALS =
        "filtered because trial is running exclusively in children's hospital. See Other Trial Matching Results for these trial matches."
    const val FOOT_NOTE_SAME_MOLECULAR_TARGET =
        "filtered due to trials recruiting nationally for the same molecular target. See Other Trial Matching Results for filtered matches."

    fun generateCohortsFromTrialsString(cohortCount: Int, trialCount: Int): String {
        val formatTrialCount = formatCountWithLabel(trialCount, "trial")
        return when {
            trialCount > cohortCount -> throw IllegalStateException("Trial count > cohort count - which should not be possible")
            cohortCount > 0 && cohortCount == trialCount -> "($formatTrialCount)"
            cohortCount > 0 -> "(${formatCountWithLabel(cohortCount, "cohort")} from $formatTrialCount)"
            else -> "(0 trials)"
        }
    }

    fun formatCountWithLabel(count: Int, word: String): String {
        return "$count $word${if (count > 1) "s" else ""}"
    }
}