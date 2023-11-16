package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.CTCDatabaseValidationError
import com.hartwig.actin.trial.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import org.apache.logging.log4j.LogManager

object CohortStatusResolver {

    private val LOGGER = LogManager.getLogger(CohortStatusResolver::class.java)

    fun resolve(
        entries: List<CTCDatabaseEntry>,
        configuredCohortIds: CohortDefinitionConfig
    ): Triple<InterpretedCohortStatus, List<CohortDefinitionValidationError>, List<CTCDatabaseValidationError>> {
        val entriesByCohortId = entries.filter { it.cohortId != null }.associateBy { it.cohortId!!.toInt() }
        val (matches, missingCohortErrors) = findEntriesByCohortIds(entriesByCohortId, configuredCohortIds)

        if (!hasValidCTCDatabaseMatches(matches)) {
            return Triple(closedWithoutSlots(), emptyList(), matches.filterNotNull()
                .map { CTCDatabaseValidationError(it, "Invalid cohort IDs configured for cohort") })
        }
        val (status, matchErrors) = interpretValidMatches(matches.filterNotNull(), entriesByCohortId)
        return Triple(status, missingCohortErrors, matchErrors)
    }

    internal fun hasValidCTCDatabaseMatches(matches: List<CTCDatabaseEntry?>): Boolean {
        val nonNullMatches = matches.filterNotNull()
        return matches.size == nonNullMatches.size && (isSingleParent(nonNullMatches) || isListOfChildren(nonNullMatches))
    }

    private fun interpretValidMatches(
        matches: List<CTCDatabaseEntry>,
        entriesByCohortId: Map<Int, CTCDatabaseEntry>
    ): Pair<InterpretedCohortStatus, List<CTCDatabaseValidationError>> {
        val validationErrors = mutableListOf<CTCDatabaseValidationError>()
        if (isSingleParent(matches)) {
            return fromEntry(matches[0])
        } else if (isListOfChildren(matches)) {
            val statuses = matches.map(::fromEntry)
            validationErrors.addAll(statuses.map { it.second }.flatten())
            val bestChildEntry = statuses.map { it.first }.maxWith(InterpretedCohortStatusComparator())
            val firstParentId = matches[0].cohortParentId
            if (matches.size > 1) {
                if (matches.any { it.cohortParentId != firstParentId }) {
                    validationErrors.add(CTCDatabaseValidationError(matches[0], "Multiple parents found for single set of children"))
                }
            }
            val parentEntry = fromEntry(entriesByCohortId[firstParentId]!!).first
            if (bestChildEntry.open && !parentEntry.open) {
                validationErrors.add(
                    CTCDatabaseValidationError(
                        matches[0],
                        " Best child from IDs '${matches.map { it.cohortId }}' is open while parent with ID '$firstParentId' is closed"
                    )
                )
            }

            if (bestChildEntry.slotsAvailable && !parentEntry.slotsAvailable) {
                validationErrors.add(
                    CTCDatabaseValidationError(
                        matches[0],
                        " Best child from IDs '${matches.map { it.cohortId }}' has slots available while parent with ID '$firstParentId' has no slots available",
                    )
                )
            }
            return bestChildEntry to validationErrors
        }
        throw IllegalStateException("Unexpected set of CTC database matches: $matches")
    }

    private fun isSingleParent(matches: List<CTCDatabaseEntry>): Boolean {
        return matches.size == 1 && !isChild(matches[0])
    }

    private fun isListOfChildren(matches: List<CTCDatabaseEntry>): Boolean {
        return matches.isNotEmpty() && matches.all(::isChild)
    }

    private fun isChild(entry: CTCDatabaseEntry): Boolean {
        return entry.cohortParentId != null
    }

    private fun findEntriesByCohortIds(
        entriesByCohortId: Map<Int, CTCDatabaseEntry>,
        cohortDefinitionConfig: CohortDefinitionConfig
    ): Pair<List<CTCDatabaseEntry?>, List<CohortDefinitionValidationError>> {
        val entriesAndValidationErrors = cohortDefinitionConfig.ctcCohortIds.map { it.toInt() }.toSet().map { cohortId ->
            entriesByCohortId[cohortId] to if (!entriesByCohortId.contains(cohortId)) {
                CohortDefinitionValidationError(
                    cohortDefinitionConfig,
                    " Could not find CTC database entry with cohort ID '$cohortId'"
                )
            } else {
                null
            }
        }
        return entriesAndValidationErrors.map { it.first } to entriesAndValidationErrors.map { it.second }.filterNotNull()
    }

    private fun fromEntry(entry: CTCDatabaseEntry): Pair<InterpretedCohortStatus, List<CTCDatabaseValidationError>> {
        val cohortStatus = entry.cohortStatus
            ?: return closedWithoutSlots() to listOf(
                CTCDatabaseValidationError(
                    entry,
                    "No cohort status available in CTC for cohort"
                )
            )

        val status: CTCStatus = CTCStatus.fromStatusString(cohortStatus)
        val slotsAvailable: Boolean = entry.cohortSlotsNumberAvailable != null && entry.cohortSlotsNumberAvailable > 0

        return InterpretedCohortStatus(
            open = status == CTCStatus.OPEN,
            slotsAvailable = slotsAvailable
        ) to if (status == CTCStatus.UNINTERPRETABLE) listOf(
            CTCDatabaseValidationError(
                entry,
                "Uninterpretable cohort status"
            )
        ) else emptyList()
    }

    private fun closedWithoutSlots(): InterpretedCohortStatus {
        return InterpretedCohortStatus(open = false, slotsAvailable = false)
    }
}