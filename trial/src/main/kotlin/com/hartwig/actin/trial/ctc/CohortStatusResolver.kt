package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.CTCDatabaseValidationError
import com.hartwig.actin.trial.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry

object CohortStatusResolver {

    fun resolve(
        entries: List<CTCDatabaseEntry>,
        configuredCohortIds: CohortDefinitionConfig
    ): CohortStatusInterpretation {
        val entriesByCohortId = entries.filter { it.cohortId != null }.associateBy { it.cohortId!!.toInt() }
        val (matches, missingCohortErrors) = findEntriesByCohortIds(entriesByCohortId, configuredCohortIds)

        if (!hasValidCTCDatabaseMatches(matches)) {
            return CohortStatusInterpretation(
                closedWithoutSlots(),
                missingCohortErrors + CohortDefinitionValidationError(configuredCohortIds, "Invalid cohort IDs configured for cohort"),
                emptyList()
            )
        }
        val (status, matchErrors) = interpretValidMatches(matches.filterNotNull(), entriesByCohortId)
        return CohortStatusInterpretation(status, missingCohortErrors, matchErrors)
    }

    internal fun hasValidCTCDatabaseMatches(matches: List<CTCDatabaseEntry?>): Boolean {
        val nonNullMatches = matches.filterNotNull()
        return matches.size == nonNullMatches.size && (isSingleParent(nonNullMatches) || isListOfChildren(nonNullMatches))
    }

    private fun interpretValidMatches(
        matches: List<CTCDatabaseEntry>,
        entriesByCohortId: Map<Int, CTCDatabaseEntry>
    ): Pair<InterpretedCohortStatus, List<CTCDatabaseValidationError>> {
        if (isSingleParent(matches)) {
            return fromEntry(matches[0])
        } else if (isListOfChildren(matches)) {
            val statuses = matches.map(::fromEntry)
            val statusValidationErrors = statuses.map { it.second }.flatten()
            val bestChildEntry = statuses.map { it.first }.maxWith(InterpretedCohortStatusComparator())
            val firstParentId = matches[0].cohortParentId
            val multipleParentValidationErrors = matches.find { it.cohortParentId != firstParentId }
                ?.let { CTCDatabaseValidationError(it, "Multiple parents found for single set of children") }
            val parentEntry = fromEntry(entriesByCohortId[firstParentId]!!).first
            val closedParentOpenChildValidationError = if (bestChildEntry.open && !parentEntry.open) {
                CTCDatabaseValidationError(
                    matches[0],
                    "Best child from IDs '${matches.map { it.cohortId }}' is open while parent with ID '$firstParentId' is closed"
                )
            } else null

            val noSlotsParentHasSlotsChildValidationError = if (bestChildEntry.slotsAvailable && !parentEntry.slotsAvailable) {
                CTCDatabaseValidationError(
                    matches[0],
                    "Best child from IDs '${matches.map { it.cohortId }}' has slots available while parent with ID '$firstParentId' has no slots available",
                )
            } else null
            return bestChildEntry to (statusValidationErrors + multipleParentValidationErrors + closedParentOpenChildValidationError + noSlotsParentHasSlotsChildValidationError).filterNotNull()
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
        val entriesAndValidationErrors = cohortDefinitionConfig.externalCohortIds.map(String::toInt).distinct().map { cohortId ->
            entriesByCohortId[cohortId] to if (entriesByCohortId.contains(cohortId)) null else {
                CohortDefinitionValidationError(
                    cohortDefinitionConfig,
                    "Could not find CTC database entry with cohort ID '$cohortId'"
                )
            }
        }
        return entriesAndValidationErrors.map { it.first } to entriesAndValidationErrors.mapNotNull { it.second }
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