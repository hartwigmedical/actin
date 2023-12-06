package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

object TreatmentHistoryEntryFunctions {
    private val yearMonthComparatorNullsFirst = nullableYearMonthComparator(Comparator.nullsFirst(Comparator.naturalOrder<Int>()))
    private val yearMonthComparatorNullsLast = nullableYearMonthComparator(Comparator.nullsLast(Comparator.naturalOrder<Int>()))

    fun portionOfTreatmentHistoryEntryMatchingPredicate(
        entry: TreatmentHistoryEntry,
        predicate: (Treatment) -> Boolean
    ): TreatmentHistoryEntry? {
        val baseMatches = entry.treatments().any(predicate)
        val additionalStages = entry.treatmentHistoryDetails()?.let { details ->
            listOfNotNull(details.switchToTreatments(), listOfNotNull(details.maintenanceTreatment())).flatten()
        } ?: emptyList()

        val matchingStages = additionalStages.dropWhile { !predicate.invoke(it.treatment()) }
            .dropLastWhile { !predicate.invoke(it.treatment()) }

        return if (matchingStages.isNotEmpty()) {
            val (matchingStageStopYear, matchingStageStopMonth) = additionalStages.takeLastWhile { !predicate.invoke(it.treatment()) }
                .maxWithOrNull(yearMonthComparatorNullsFirst)
                ?.let { Pair(it.startYear(), it.startMonth()) }
                ?: entry.treatmentHistoryDetails()!!.let { details -> Pair(details.stopYear(), details.stopMonth()) }

            val knownStageCycles = matchingStages.mapNotNull(TreatmentStage::cycles).ifEmpty { null }?.sum()

            if (!baseMatches) {
                val (matchingStageStartYear, matchingStageStartMonth) = matchingStages.minWith(yearMonthComparatorNullsLast)
                    .let { Pair(it.startYear(), it.startMonth()) }
                overrideEntry(
                    entry,
                    matchingStages.map(TreatmentStage::treatment),
                    matchingStageStartYear,
                    matchingStageStartMonth,
                    matchingStageStopYear,
                    matchingStageStopMonth,
                    knownStageCycles
                )
            } else {
                val knownCycles = listOfNotNull(knownStageCycles, entry.treatmentHistoryDetails()?.cycles()).ifEmpty { null }?.sum()
                overrideEntry(
                    entry,
                    matchingStages.map(TreatmentStage::treatment) + entry.treatments(),
                    entry.startYear(),
                    entry.startMonth(),
                    matchingStageStopYear,
                    matchingStageStopMonth,
                    knownCycles
                )
            }
        } else if (baseMatches) {
            val details = if (additionalStages.isEmpty()) entry.treatmentHistoryDetails() else {
                val (stopYear, stopMonth) = additionalStages.minWith(yearMonthComparatorNullsLast)
                    .let { Pair(it.startYear(), it.startMonth()) }

                overrideTreatmentHistoryDetails(
                    entry.treatmentHistoryDetails(), stopYear, stopMonth, entry.treatmentHistoryDetails()?.cycles()
                )
            }

            ImmutableTreatmentHistoryEntry.copyOf(entry).withTreatmentHistoryDetails(details)
        } else {
            null
        }
    }

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

    private fun overrideTreatmentHistoryDetails(
        details: TreatmentHistoryDetails?, stopYear: Int?, stopMonth: Int?, cycles: Int?
    ): ImmutableTreatmentHistoryDetails {
        return details?.let {
            ImmutableTreatmentHistoryDetails.copyOf(it)
                .withStopYear(stopYear)
                .withStopMonth(stopMonth)
                .withMaintenanceTreatment(null)
                .withSwitchToTreatments(emptyList())
                .withCycles(cycles)
        } ?: ImmutableTreatmentHistoryDetails.builder().stopYear(stopYear).stopMonth(stopMonth).cycles(cycles).build()
    }

    private fun overrideEntry(
        entry: TreatmentHistoryEntry,
        treatments: List<Treatment>,
        startYear: Int?,
        startMonth: Int?,
        stopYear: Int?,
        stopMonth: Int?,
        cycles: Int?
    ): ImmutableTreatmentHistoryEntry {
        return ImmutableTreatmentHistoryEntry.copyOf(entry)
            .withTreatments(treatments)
            .withStartYear(startYear)
            .withStartMonth(startMonth)
            .withTreatmentHistoryDetails(
                overrideTreatmentHistoryDetails(entry.treatmentHistoryDetails(), stopYear, stopMonth, cycles)
            )
    }

    private fun nullableYearMonthComparator(nullSafeComparator: java.util.Comparator<Int?>): java.util.Comparator<TreatmentStage> =
        Comparator.comparing(TreatmentStage::startYear, nullSafeComparator)
            .thenComparing(TreatmentStage::startMonth, nullSafeComparator)
}