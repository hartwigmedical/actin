package com.hartwig.actin.treatment.trial

object TrialConfigDatabaseUtil {
    private const val COHORT_SEPARATOR = ","
    private const val REFERENCE_ID_SEPARATOR = ","
    private const val ALL_COHORTS = "all"

    fun toReferenceIds(referenceIdsString: String): Set<String> {
        return if (referenceIdsString.isEmpty()) emptySet() else toSet(referenceIdsString, REFERENCE_ID_SEPARATOR)
    }

    fun toCohorts(appliesToCohortString: String): Set<String> {
        return when {
            appliesToCohortString.isEmpty() -> {
                throw IllegalArgumentException("Empty argument appliesToCohortString!")
            }

            appliesToCohortString != ALL_COHORTS -> {
                toSet(appliesToCohortString, COHORT_SEPARATOR)
            }

            else -> {
                emptySet()
            }
        }
    }

    private fun toSet(string: String, delimiter: String): Set<String> {
        return string.split(delimiter).map(String::trim).dropLastWhile { it.isEmpty() }.toSet()
    }
}