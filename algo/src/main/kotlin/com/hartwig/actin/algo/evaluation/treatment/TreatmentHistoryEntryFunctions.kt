package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage

object TreatmentHistoryEntryFunctions {

    private data class NullableYearMonth(val year: Int?, val month: Int?)

    private val nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder<Int>())
    private val stageDateComparatorNullsLast = Comparator.comparing(TreatmentStage::startYear, nullSafeComparator)
        .thenComparing(TreatmentStage::startMonth, nullSafeComparator)

    data class TreatmentHistoryEvaluation(
        val matchingDrugsWithPD: Set<Drug> = emptySet(),
        val matchingDrugs: Set<Drug> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val possibleTrialMatch: Boolean = false,
        val matchesWithToxicity: Boolean = false
    )

    fun TreatmentHistoryEntry.containsTreatment(treatmentNameToFind: String) =
        allTreatments().any { it.name.equals(treatmentNameToFind, ignoreCase = true) }

    fun evaluateIfDrugHadPDResponse(treatmentHistory: List<TreatmentHistoryEntry>, drugsToMatch: Set<Drug>): TreatmentHistoryEvaluation {
        val allowTrialMatches = drugsToMatch.map(Drug::category).all(TrialFunctions::categoryAllowsTrialMatches)

        return treatmentHistory.map { entry ->
            val categoriesToMatch = drugsToMatch.map(Drug::category).toSet()
            val isPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(entry)
            val matchingDrugs = entry.allTreatments().flatMap {
                (it as? DrugTreatment)?.drugs?.intersect(drugsToMatch) ?: emptyList()
            }.toSet()
            val possibleTrialMatch =
                entry.isTrial && (entry.categories().isEmpty() || entry.categories().intersect(categoriesToMatch).isNotEmpty())
                        && allowTrialMatches
            val matchesWithToxicity = entry.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
            if (matchingDrugs.isNotEmpty()) {
                TreatmentHistoryEvaluation(
                    matchingDrugsWithPD = if (isPD == true) matchingDrugs else emptySet(),
                    matchingDrugs = matchingDrugs,
                    matchesWithUnclearPD = isPD == null,
                    possibleTrialMatch = possibleTrialMatch,
                    matchesWithToxicity = matchesWithToxicity
                )
            } else {
                TreatmentHistoryEvaluation(possibleTrialMatch = possibleTrialMatch)
            }
        }.fold(TreatmentHistoryEvaluation()) { acc, result ->
            TreatmentHistoryEvaluation(
                matchingDrugsWithPD = acc.matchingDrugsWithPD + result.matchingDrugsWithPD,
                matchingDrugs = acc.matchingDrugs + result.matchingDrugs,
                matchesWithUnclearPD = acc.matchesWithUnclearPD || result.matchesWithUnclearPD,
                possibleTrialMatch = acc.possibleTrialMatch || result.possibleTrialMatch,
                matchesWithToxicity = acc.matchesWithToxicity || result.matchesWithToxicity
            )
        }
    }

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