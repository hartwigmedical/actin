package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import org.apache.logging.log4j.LogManager

object CohortStatusResolver {

    private val LOGGER = LogManager.getLogger(CohortStatusResolver::class.java)

    fun resolve(entries: List<CTCDatabaseEntry>, configuredCohortIds: Set<Int>): InterpretedCohortStatus {
        val entriesByCohortId = entries.filter { it.cohortId != null }.associateBy { it.cohortId!!.toInt() }
        val matches: List<CTCDatabaseEntry?> = findEntriesByCohortIds(entriesByCohortId, configuredCohortIds)

        if (!hasValidCTCDatabaseMatches(matches)) {
            LOGGER.warn(" Invalid cohort IDs configured for cohort: '{}'. Assuming cohort is closed without slots", configuredCohortIds)
            return closedWithoutSlots()
        }
        return interpretValidMatches(matches.filterNotNull(), entriesByCohortId)
    }

    internal fun hasValidCTCDatabaseMatches(matches: List<CTCDatabaseEntry?>): Boolean {
        val nonNullMatches = matches.filterNotNull()
        return matches.size == nonNullMatches.size && (isSingleParent(nonNullMatches) || isListOfChildren(nonNullMatches))
    }

    private fun interpretValidMatches(
        matches: List<CTCDatabaseEntry>,
        entriesByCohortId: Map<Int, CTCDatabaseEntry>
    ): InterpretedCohortStatus {
        if (isSingleParent(matches)) {
            return fromEntry(matches[0])
        } else if (isListOfChildren(matches)) {
            val bestChildEntry = matches.map(::fromEntry).maxWith(InterpretedCohortStatusComparator())
            val firstParentId = matches[0].cohortParentId
            if (matches.size > 1) {
                if (matches.any { it.cohortParentId != firstParentId }) {
                    LOGGER.warn(" Multiple parents found for single set of children: {}", matches)
                }
            }
            val parentEntry = fromEntry(entriesByCohortId[firstParentId]!!)
            if (bestChildEntry.open && !parentEntry.open) {
                LOGGER.warn(
                    " Best child from IDs '{}' is open while parent with ID '{}' is closed",
                    matches.map { it.cohortId },
                    firstParentId
                )
            }

            if (bestChildEntry.slotsAvailable && !parentEntry.slotsAvailable) {
                LOGGER.warn(
                    " Best child from IDs '{}' has slots available while parent with ID '{}' has no slots available",
                    matches.map { it.cohortId },
                    firstParentId
                )
            }
            return bestChildEntry
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
        configuredCohortIds: Set<Int>
    ): List<CTCDatabaseEntry?> {
        return configuredCohortIds.map { cohortId ->
            if (!entriesByCohortId.contains(cohortId)) {
                LOGGER.warn(" Could not find CTC database entry with cohort ID '{}'", cohortId)
            }
            entriesByCohortId[cohortId]
        }
    }

    private fun fromEntry(entry: CTCDatabaseEntry): InterpretedCohortStatus {
        val cohortStatus = entry.cohortStatus
        if (cohortStatus == null) {
            LOGGER.warn(
                " No cohort status available in CTC for cohort with ID '{}'. Assuming cohort is closed without slots",
                entry.cohortId
            )
            return closedWithoutSlots()
        }

        val status: CTCStatus = CTCStatus.fromStatusString(cohortStatus)
        val slotsAvailable: Boolean = entry.cohortSlotsNumberAvailable != null && entry.cohortSlotsNumberAvailable > 0

        return InterpretedCohortStatus(open = status == CTCStatus.OPEN, slotsAvailable = slotsAvailable)
    }

    private fun closedWithoutSlots(): InterpretedCohortStatus {
        return InterpretedCohortStatus(open = false, slotsAvailable = false)
    }
}