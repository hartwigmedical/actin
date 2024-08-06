package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.CohortDefinitionValidationError

object CohortStatusResolver {

    fun resolve(
        entries: List<TrialStatusEntry>,
        configuredCohortIds: CohortDefinitionConfig
    ): CohortStatusInterpretation {
        val entriesByCohortId = entries.filter { it.cohortId != null }.associateBy { it.cohortId!! }

        val (matches, missingCohortErrors) = findEntriesByCohortIds(entriesByCohortId, configuredCohortIds)

        if (!hasValidTrialStatusDatabaseMatches(matches)) {
            return CohortStatusInterpretation(
                closedWithoutSlots(),
                missingCohortErrors + CohortDefinitionValidationError(configuredCohortIds, "Invalid cohort IDs configured for cohort"),
                emptyList()
            )
        }
        val (status, matchErrors) = interpretValidMatches(matches.filterNotNull(), entriesByCohortId)
        return CohortStatusInterpretation(status, missingCohortErrors, matchErrors)
    }

    internal fun hasValidTrialStatusDatabaseMatches(matches: List<TrialStatusEntry?>): Boolean {
        val nonNullMatches = matches.filterNotNull()
        return matches.size == nonNullMatches.size && (isSingleParent(nonNullMatches) || isListOfChildren(nonNullMatches))
    }

    private tailrec fun collectAncestorsFor(
        entry: TrialStatusEntry, entriesByCohortId: Map<String, TrialStatusEntry>, knownAncestorIds: List<String> = emptyList()
    ): List<String> {
        if (entry.cohortParentId == null) {
            return knownAncestorIds
        }
        return collectAncestorsFor(entriesByCohortId[entry.cohortParentId]!!, entriesByCohortId, knownAncestorIds + entry.cohortParentId)
    }

    private fun findCommonAncestor(matches: List<TrialStatusEntry>, entriesByCohortId: Map<String, TrialStatusEntry>): String? {
        val firstPedigree = collectAncestorsFor(matches.first(), entriesByCohortId)
        val pedigrees: List<Set<String>> = matches.drop(1).map { match -> collectAncestorsFor(match, entriesByCohortId).toSet() }
        return firstPedigree.find { ancestor -> pedigrees.all { pedigree -> pedigree.contains(ancestor) } }
    }

    private fun interpretValidMatches(
        matches: List<TrialStatusEntry>,
        entriesByCohortId: Map<String, TrialStatusEntry>
    ): Pair<InterpretedCohortStatus, List<TrialStatusDatabaseValidationError>> {
        if (isSingleParent(matches)) {
            return fromEntry(matches[0])
        } else if (isListOfChildren(matches)) {
            val statuses = matches.map(::fromEntry)
            val statusValidationErrors = statuses.map { it.second }.flatten()
            val bestChildEntry = statuses.map { it.first }.maxWith(InterpretedCohortStatusComparator())
            val commonAncestorId = findCommonAncestor(matches, entriesByCohortId)
            val multipleParentValidationErrors: List<TrialStatusDatabaseValidationError> = if (commonAncestorId == null) {
                listOf(
                    TrialStatusDatabaseValidationError(
                        matches.first(),
                        "No common ancestor cohort found for cohorts [${matches.mapNotNull(TrialStatusEntry::cohortId).joinToString(", ")}]"
                    )
                )
            } else emptyList()
            val firstParentId = matches[0].cohortParentId
            val parentEntry = fromEntry(entriesByCohortId[firstParentId]!!).first
            val closedParentOpenChildValidationError = if (bestChildEntry.open && !parentEntry.open) {
                TrialStatusDatabaseValidationError(
                    matches[0],
                    "Best child from IDs '${matches.map { it.cohortId }}' is open while parent with ID '$firstParentId' is closed"
                )
            } else null

            val noSlotsParentHasSlotsChildValidationError = if (bestChildEntry.slotsAvailable && !parentEntry.slotsAvailable) {
                TrialStatusDatabaseValidationError(
                    matches[0],
                    "Best child from IDs '${matches.map { it.cohortId }}' has slots available while parent with ID '$firstParentId' has no slots available",
                )
            } else null
            return bestChildEntry to (statusValidationErrors + multipleParentValidationErrors + closedParentOpenChildValidationError + noSlotsParentHasSlotsChildValidationError).filterNotNull()
        }
        throw IllegalStateException("Unexpected set of CTC database matches: $matches")
    }

    private fun isSingleParent(matches: List<TrialStatusEntry>): Boolean {
        return matches.size == 1 && !isChild(matches[0])
    }

    private fun isListOfChildren(matches: List<TrialStatusEntry>): Boolean {
        return matches.isNotEmpty() && matches.all(::isChild)
    }

    private fun isChild(entry: TrialStatusEntry): Boolean {
        return entry.cohortParentId != null
    }

    private fun findEntriesByCohortIds(
        entriesByCohortId: Map<String, TrialStatusEntry>,
        cohortDefinitionConfig: CohortDefinitionConfig
    ): Pair<List<TrialStatusEntry?>, List<CohortDefinitionValidationError>> {
        val entriesAndValidationErrors = cohortDefinitionConfig.externalCohortIds.distinct().map { cohortId ->
            entriesByCohortId[cohortId] to if (entriesByCohortId.contains(cohortId)) null else {
                CohortDefinitionValidationError(
                    cohortDefinitionConfig,
                    "Could not find trial status database entry with cohort ID '$cohortId'"
                )
            }
        }
        return entriesAndValidationErrors.map { it.first } to entriesAndValidationErrors.mapNotNull { it.second }
    }

    private fun fromEntry(entry: TrialStatusEntry): Pair<InterpretedCohortStatus, List<TrialStatusDatabaseValidationError>> {
        val cohortStatus = entry.cohortStatus
            ?: return closedWithoutSlots() to listOf(
                TrialStatusDatabaseValidationError(
                    entry,
                    "No cohort status available in trial status database for cohort"
                )
            )

        val slotsAvailable: Boolean = entry.cohortSlotsNumberAvailable != null && entry.cohortSlotsNumberAvailable > 0

        return InterpretedCohortStatus(
            open = cohortStatus == TrialStatus.OPEN,
            slotsAvailable = slotsAvailable
        ) to if (cohortStatus == TrialStatus.UNINTERPRETABLE) listOf(
            TrialStatusDatabaseValidationError(
                entry,
                "Uninterpretable cohort status"
            )
        ) else emptyList()
    }

    private fun closedWithoutSlots(): InterpretedCohortStatus {
        return InterpretedCohortStatus(open = false, slotsAvailable = false)
    }
}