package com.hartwig.actin.trial.config

import com.hartwig.actin.datamodel.trial.TrialLocation

object TrialConfigDatabaseUtil {

    private const val COMMA_SEPARATOR = ","
    private const val TRIAL_LOCATION_SEPARATOR = ":"
    private const val ALL_COHORTS = "all"
    private const val TRIAL_LOCATION_REG_EXP = "^\\d+,[^,:]+(:\\d+,[^,:]+)*$"

    fun toReferenceIds(referenceIdsString: String): Set<String> {
        return if (referenceIdsString.isEmpty()) emptySet() else toSet(referenceIdsString, COMMA_SEPARATOR)
    }

    fun toCohorts(appliesToCohortString: String): Set<String> {
        return when {
            appliesToCohortString.isEmpty() -> {
                throw IllegalArgumentException("Empty argument appliesToCohortString!")
            }

            appliesToCohortString != ALL_COHORTS -> {
                toSet(appliesToCohortString, COMMA_SEPARATOR)
            }

            else -> {
                emptySet()
            }
        }
    }

    fun validateTrialLocation(input: String?): Boolean {
        return input.isNullOrEmpty() || Regex(TRIAL_LOCATION_REG_EXP).matches(input)
    }

    fun toTrialLocation(input: String?): List<TrialLocation> {

        if (!validateTrialLocation(input)) {
            throw IllegalArgumentException("Invalid location $input")
        }

        return input.takeIf { !it.isNullOrEmpty() }?.let {
            it.split(TRIAL_LOCATION_SEPARATOR)
                .map { loc -> loc.split(COMMA_SEPARATOR).let { (id, name) -> TrialLocation(id.toInt(), name) } }
        } ?: emptyList()
    }

    private fun toSet(string: String, delimiter: String): Set<String> {
        return string.split(delimiter).map(String::trim).dropLastWhile { it.isEmpty() }.toSet()
    }
}