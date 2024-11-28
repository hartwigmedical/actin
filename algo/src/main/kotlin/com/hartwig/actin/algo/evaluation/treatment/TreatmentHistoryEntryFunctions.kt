package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage

object TreatmentHistoryEntryFunctions {

    private data class NullableYearMonth(val year: Int?, val month: Int?)

    private val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int>())
    private val stageDateComparatorNullsLast = Comparator.comparing(TreatmentStage::startYear, nullSafeComparator)
        .thenComparing(TreatmentStage::startMonth, nullSafeComparator)

    fun fullTreatmentDisplay(entry: TreatmentHistoryEntry): String {
        return entry.treatmentHistoryDetails?.let { details ->
            val switchToTreatmentDisplay = if (details.switchToTreatments.isNullOrEmpty()) "" else {
                " with switch to " + details.switchToTreatments!!.joinToString(" then ") { it.treatment.display() }
            }
            val maintenanceTreatmentDisplay = details.maintenanceTreatment?.let {
                " continued with ${details.maintenanceTreatment!!.treatment.display()} maintenance"
            } ?: ""
            entry.treatmentDisplay() + switchToTreatmentDisplay + maintenanceTreatmentDisplay
        } ?: entry.treatmentDisplay()
    }

    fun portionOfTreatmentHistoryEntryMatchingPredicate(
        entry: TreatmentHistoryEntry, predicate: (Treatment) -> Boolean
    ): TreatmentHistoryEntry? {
        val initialTreatmentStageMatches = entry.treatments.any(predicate)
        val details = entry.treatmentHistoryDetails
        val additionalTreatmentStages = (details?.switchToTreatments ?: emptyList()) + listOfNotNull(details?.maintenanceTreatment)

        val matchingAdditionalStages = dropNonMatchingStagesFromStartAndEnd(additionalTreatmentStages, predicate)

        return if (matchingAdditionalStages.isNotEmpty()) {
            val (initialMatchingTreatments, matchStartYearAndMonth, initialMatchingCycles) = if (initialTreatmentStageMatches) {
                Triple(
                    entry.treatments,
                    NullableYearMonth(entry.startYear, entry.startMonth),
                    listOfNotNull(details?.cycles)
                )
            } else {
                Triple(emptySet(), firstStartYearAndMonthFromStageList(matchingAdditionalStages), emptyList())
            }

            val matchingStageStopYearAndMonth =
                startYearAndMonthOfFirstTrailingNonMatchingStage(additionalTreatmentStages, predicate)
                    ?: NullableYearMonth(details?.stopYear, details?.stopMonth)

            createSubEntryWithMatchingTreatmentsAndDatesAndCycles(
                entry,
                matchingAdditionalStages.map(TreatmentStage::treatment) + initialMatchingTreatments,
                matchStartYearAndMonth,
                matchingStageStopYearAndMonth,
                sumOrNullIfEmpty(matchingAdditionalStages.mapNotNull(TreatmentStage::cycles) + initialMatchingCycles)
            )
        } else if (initialTreatmentStageMatches) {
            val newDetails = if (additionalTreatmentStages.isEmpty()) details else {
                val stopYearAndMonth = firstStartYearAndMonthFromStageList(additionalTreatmentStages)
                createTreatmentHistoryDetailsWithMatchingDateAndCycles(details, stopYearAndMonth, details?.cycles)
            }

            entry.copy(treatmentHistoryDetails = newDetails)
        } else {
            null
        }
    }

    private fun sumOrNullIfEmpty(list: List<Int>) = if (list.isEmpty()) null else list.sum()

    private fun firstStartYearAndMonthFromStageList(additionalTreatmentStages: List<TreatmentStage>): NullableYearMonth {
        return additionalTreatmentStages.minWith(stageDateComparatorNullsLast).let { NullableYearMonth(it.startYear, it.startMonth) }
    }

    private fun startYearAndMonthOfFirstTrailingNonMatchingStage(
        additionalStages: List<TreatmentStage>, predicate: (Treatment) -> Boolean
    ): NullableYearMonth? {
        val trailingNonMatchingStages = additionalStages.takeLastWhile { !predicate.invoke(it.treatment) }
        return if (trailingNonMatchingStages.isEmpty()) null else firstStartYearAndMonthFromStageList(trailingNonMatchingStages)
    }

    private fun dropNonMatchingStagesFromStartAndEnd(
        additionalStages: List<TreatmentStage>, predicate: (Treatment) -> Boolean
    ) = additionalStages.dropWhile { !predicate.invoke(it.treatment) }
        .dropLastWhile { !predicate.invoke(it.treatment) }

    private fun createTreatmentHistoryDetailsWithMatchingDateAndCycles(
        details: TreatmentHistoryDetails?, stopYearMonth: NullableYearMonth, cycles: Int?
    ): TreatmentHistoryDetails {
        return details?.copy(
            stopYear = stopYearMonth.year,
            stopMonth = stopYearMonth.month,
            maintenanceTreatment = null,
            switchToTreatments = emptyList(),
            cycles = cycles
        ) ?: TreatmentHistoryDetails(
            stopYear = stopYearMonth.year,
            stopMonth = stopYearMonth.month,
            ongoingAsOf = null,
            cycles = cycles,
            bestResponse = null,
            stopReason = null,
            stopReasonDetail = null,
            switchToTreatments = emptyList(),
            maintenanceTreatment = null
        )
    }

    private fun createSubEntryWithMatchingTreatmentsAndDatesAndCycles(
        entry: TreatmentHistoryEntry,
        treatments: List<Treatment>,
        startYearMonth: NullableYearMonth,
        stopYearMonth: NullableYearMonth,
        cycles: Int?
    ): TreatmentHistoryEntry {
        return entry.copy(
            treatments = treatments.toSet(),
            startYear = startYearMonth.year,
            startMonth = startYearMonth.month,
            treatmentHistoryDetails = createTreatmentHistoryDetailsWithMatchingDateAndCycles(
                entry.treatmentHistoryDetails, stopYearMonth, cycles
            )
        )
    }
}