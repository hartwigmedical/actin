package com.hartwig.actin.treatment.ctc

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import org.apache.logging.log4j.LogManager

object CTCDatabaseEntryInterpreter {
    private val LOGGER = LogManager.getLogger(CohortStatusInterpreter::class.java)

    @VisibleForTesting
    fun hasValidCTCDatabaseMatches(matches: List<CTCDatabaseEntry?>): Boolean {
        val nonNullMatches = matches.filterNotNull()
        return matches.size == nonNullMatches.size && (isSingleParent(nonNullMatches) || isListOfChildren(nonNullMatches))
    }

    fun consolidatedCohortStatus(entries: List<CTCDatabaseEntry>, cohortConfig: CohortDefinitionConfig): InterpretedCohortStatus {
        val entriesByCohortId = entries.filter { it.cohortId != null }.associateBy { it.cohortId!!.toInt() }
        val configuredCohortIds = cohortConfig.ctcCohortIds.map { it.toInt() }.toSet()
        val matches: List<CTCDatabaseEntry?> = findEntriesByCohortIds(entriesByCohortId, configuredCohortIds)

        if (!hasValidCTCDatabaseMatches(matches)) {
            LOGGER.warn(
                "Invalid cohort IDs configured for cohort '{}' of trial '{}': '{}'. Assuming cohort is closed without slots",
                cohortConfig.cohortId,
                cohortConfig.trialId,
                configuredCohortIds
            )
            return closedWithoutSlots()
        }
        return interpretValidMatches(matches.filterNotNull(), entriesByCohortId)
    }

    private fun interpretValidMatches(
        matches: List<CTCDatabaseEntry>,
        entriesByCohortId: Map<Int, CTCDatabaseEntry>
    ): InterpretedCohortStatus {
        if (isSingleParent(matches)) {
            return fromEntry(matches[0])
        } else if (isListOfChildren(matches)) {
            val best = matches.map(::fromEntry).maxWith(InterpretedCohortStatusComparator())
            val firstParentId = matches[0].cohortParentId
            if (matches.size > 1) {
                if (matches.any { it.cohortParentId != firstParentId }) {
                    LOGGER.warn("Multiple parents found for single set of children: {}", matches)
                }
            }
            if (best != fromEntry(entriesByCohortId[firstParentId]!!)) {
                LOGGER.warn(
                    "Inconsistent status between best child and parent cohort in CTC for cohort with parent ID '{}'",
                    matches[0].cohortParentId
                )
            }
            return best
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
                LOGGER.warn("Could not find CTC database entry with cohort ID '{}'", cohortId)
            }
            entriesByCohortId[cohortId]
        }
    }

    private fun fromEntry(entry: CTCDatabaseEntry): InterpretedCohortStatus {
        val cohortStatus = entry.cohortStatus
        if (cohortStatus == null) {
            LOGGER.warn(
                "No cohort status available in CTC for cohort with ID '{}'. Assuming cohort is closed without slots",
                entry.cohortId
            )
            return closedWithoutSlots()
        }
        val numberSlotsAvailable = entry.cohortSlotsNumberAvailable
        val status: CTCStatus = CTCStatus.fromStatusString(cohortStatus)
        val slotsAvailable: Boolean = if (numberSlotsAvailable == null && status == CTCStatus.OPEN) {
            LOGGER.warn(
                "No data available on number of slots for open cohort with ID '{}'. Assuming no slots available",
                entry.cohortId
            )
            false
        } else {
            numberSlotsAvailable != null && numberSlotsAvailable > 0
        }
        return InterpretedCohortStatus(open = status == CTCStatus.OPEN, slotsAvailable = slotsAvailable)
    }

    private fun closedWithoutSlots(): InterpretedCohortStatus {
        return InterpretedCohortStatus(open = false, slotsAvailable = false)
    }
}