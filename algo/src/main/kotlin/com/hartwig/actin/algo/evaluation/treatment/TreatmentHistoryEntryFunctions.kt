package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

private data class NullableYearMonth(val year: Int?, val month: Int?)

object TreatmentHistoryEntryFunctions {
    private val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int>())
    private val yearMonthComparatorNullsLast = Comparator.comparing(TreatmentStage::startYear, nullSafeComparator)
        .thenComparing(TreatmentStage::startMonth, nullSafeComparator)

    fun fullTreatmentDisplay(entry: TreatmentHistoryEntry): String {
        return entry.treatmentHistoryDetails()?.let { details ->
            val switchToTreatmentDisplay = if (details.switchToTreatments().isNullOrEmpty()) "" else {
                " with switch to " + details.switchToTreatments()!!.joinToString(" then ") { it.treatment().display() }
            }
            val maintenanceTreatmentDisplay = details.maintenanceTreatment()?.let {
                " continued with ${details.maintenanceTreatment()!!.treatment().display()} maintenance"
            } ?: ""
            entry.treatmentDisplay() + switchToTreatmentDisplay + maintenanceTreatmentDisplay
        } ?: entry.treatmentDisplay()
    }

    fun portionOfTreatmentHistoryEntryMatchingPredicate(
        entry: TreatmentHistoryEntry, predicate: (Treatment) -> Boolean
    ): TreatmentHistoryEntry? {
        val initialTreatmentStageMatches = entry.treatments().any(predicate)
        val additionalTreatmentStages = (entry.treatmentHistoryDetails()?.switchToTreatments() ?: emptyList()) + listOfNotNull(
            entry.treatmentHistoryDetails()?.maintenanceTreatment()
        )

        val matchingAdditionalStages = dropNonMatchingStagesFromStartAndEnd(additionalTreatmentStages, predicate)

        return if (matchingAdditionalStages.isNotEmpty()) {
            val (initialMatchingTreatments, matchStartYearAndMonth, initialMatchingCycles) = if (initialTreatmentStageMatches) {
                Triple(
                    entry.treatments(),
                    NullableYearMonth(entry.startYear(), entry.startMonth()),
                    listOfNotNull(entry.treatmentHistoryDetails()?.cycles())
                )
            } else {
                Triple(emptySet(), firstStartYearAndMonthFromStageList(matchingAdditionalStages), emptyList())
            }

            val matchingStageStopYearAndMonth =
                startYearAndMonthOfFirstTrailingNonMatchingStage(additionalTreatmentStages, predicate)
                    ?: NullableYearMonth(entry.treatmentHistoryDetails()!!.stopYear(), entry.treatmentHistoryDetails()!!.stopMonth())

            createSubEntryWithMatchingTreatmentsAndDatesAndCycles(
                entry,
                matchingAdditionalStages.map(TreatmentStage::treatment) + initialMatchingTreatments,
                matchStartYearAndMonth,
                matchingStageStopYearAndMonth,
                sumOrNullIfEmpty(matchingAdditionalStages.mapNotNull(TreatmentStage::cycles) + initialMatchingCycles)
            )
        } else if (initialTreatmentStageMatches) {
            val details = if (additionalTreatmentStages.isEmpty()) entry.treatmentHistoryDetails() else {
                val stopYearAndMonth = firstStartYearAndMonthFromStageList(additionalTreatmentStages)

                createTreatmentHistoryDetailsWithMatchingDateAndCycles(
                    entry.treatmentHistoryDetails(), stopYearAndMonth, entry.treatmentHistoryDetails()?.cycles()
                )
            }

            ImmutableTreatmentHistoryEntry.copyOf(entry).withTreatmentHistoryDetails(details)
        } else {
            null
        }
    }

    private fun sumOrNullIfEmpty(list: List<Int>) = if (list.isEmpty()) null else list.sum()

    private fun firstStartYearAndMonthFromStageList(additionalTreatmentStages: List<TreatmentStage>): NullableYearMonth {
        return additionalTreatmentStages.minWith(yearMonthComparatorNullsLast).let { NullableYearMonth(it.startYear(), it.startMonth()) }
    }

    private fun startYearAndMonthOfFirstTrailingNonMatchingStage(
        additionalStages: List<TreatmentStage>, predicate: (Treatment) -> Boolean
    ): NullableYearMonth? {
        val trailingNonMatchingStages = additionalStages.takeLastWhile { !predicate.invoke(it.treatment()) }
        return if (trailingNonMatchingStages.isEmpty()) null else firstStartYearAndMonthFromStageList(trailingNonMatchingStages)
    }

    private fun dropNonMatchingStagesFromStartAndEnd(
        additionalStages: List<TreatmentStage>, predicate: (Treatment) -> Boolean
    ) = additionalStages.dropWhile { !predicate.invoke(it.treatment()) }
        .dropLastWhile { !predicate.invoke(it.treatment()) }

    private fun createTreatmentHistoryDetailsWithMatchingDateAndCycles(
        details: TreatmentHistoryDetails?, stopYearMonth: NullableYearMonth, cycles: Int?
    ): ImmutableTreatmentHistoryDetails {
        return details?.let {
            ImmutableTreatmentHistoryDetails.copyOf(it)
                .withStopYear(stopYearMonth.year)
                .withStopMonth(stopYearMonth.month)
                .withMaintenanceTreatment(null)
                .withSwitchToTreatments(emptyList())
                .withCycles(cycles)
        } ?: ImmutableTreatmentHistoryDetails.builder().stopYear(stopYearMonth.year).stopMonth(stopYearMonth.month).cycles(cycles).build()
    }

    private fun createSubEntryWithMatchingTreatmentsAndDatesAndCycles(
        entry: TreatmentHistoryEntry,
        treatments: List<Treatment>,
        startYearMonth: NullableYearMonth,
        stopYearMonth: NullableYearMonth,
        cycles: Int?
    ): ImmutableTreatmentHistoryEntry {
        return ImmutableTreatmentHistoryEntry.copyOf(entry)
            .withTreatments(treatments)
            .withStartYear(startYearMonth.year)
            .withStartMonth(startYearMonth.month)
            .withTreatmentHistoryDetails(
                createTreatmentHistoryDetailsWithMatchingDateAndCycles(entry.treatmentHistoryDetails(), stopYearMonth, cycles)
            )
    }
}