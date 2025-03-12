package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.InterpretedCohort
import java.time.LocalDate
import java.util.*
import kotlin.Comparator

data class ExternalTrialSummary(
    val nctId: String,
    val title: String,
    val countries: SortedSet<CountryDetails>,
    val actinMolecularEvents: SortedSet<String>,
    val sourceMolecularEvents: SortedSet<String>,
    val applicableCancerTypes: SortedSet<CancerType>,
    val url: String
)

data class EventWithExternalTrial(val event: String, val trial: ExternalTrial)

private fun countryNames(it: ExternalTrialSummary) = it.countries.map { c -> c.country }

private fun hospitalsNamesForCountry(trial: ExternalTrialSummary, country: Country) =
    trial.countries.firstOrNull { it.country == country }?.hospitalsPerCity?.flatMap { it.value }?.toSet()
        ?: throw IllegalArgumentException("Country not found")

fun Set<ExternalTrialSummary>.filterInternalTrials(internalTrials: Set<TrialMatch>): Set<ExternalTrialSummary> {
    val internalIds = internalTrials.map { it.identification.nctId }.toSet()
    return this.filter { it.nctId !in internalIds }.toSet()
}

fun Set<ExternalTrialSummary>.filterInCountry(country: Country): Set<ExternalTrialSummary> {
    return this.filter { country in countryNames(it).toSet() }.toSet()
}

fun Set<ExternalTrialSummary>.filterNotInCountry(country: Country): Set<ExternalTrialSummary> {
    return this.filter { country !in countryNames(it) }.toSet()
}

fun Set<ExternalTrialSummary>.filterExclusivelyInChildrensHospitalsInReferenceCountry(
    birthYear: Int,
    referenceDate: LocalDate,
    countryOfReference: Country
): Set<ExternalTrialSummary> {
    val isYoungAdult = referenceDate.year - birthYear < 40
    return this.filter { trial ->
        val allHospitalsAreChildrensInReferenceCountry =
            hospitalsNamesForCountry(trial, countryOfReference).all { it.isChildrensHospital == true }
        !allHospitalsAreChildrensInReferenceCountry || isYoungAdult
    }.toSet()
}

fun Set<ExternalTrialSummary>.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(internalEvaluatedCohorts: List<InterpretedCohort>): Set<ExternalTrialSummary> {
    return filterMolecularCriteriaAlreadyPresent(internalEvaluatedCohorts.flatMap { it.molecularEvents }.toSet())
}

fun Set<ExternalTrialSummary>.filterMolecularCriteriaAlreadyPresentInTrials(trials: Set<ExternalTrialSummary>): Set<ExternalTrialSummary> {
    return filterMolecularCriteriaAlreadyPresent(trials.flatMap { it.actinMolecularEvents }.toSet())
}

private fun Set<ExternalTrialSummary>.filterMolecularCriteriaAlreadyPresent(presentEvents: Set<String>): Set<ExternalTrialSummary> {
    return filter {
        it.actinMolecularEvents.subtract(presentEvents).isNotEmpty()
    }.toSet()
}


object ExternalTrialSummarizer {

    fun summarize(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>): Set<ExternalTrialSummary> {
        return externalTrialsPerEvent.flatMap {
            it.value.map { trial -> EventWithExternalTrial(it.key, trial) }
        }.groupBy { ewt -> ewt.trial.nctId }.map { entry ->
            val countries = entry.value.flatMap { ewt -> ewt.trial.countries }
            val trial = entry.value.first().trial
            ExternalTrialSummary(
                nctId = entry.key,
                title = trial.title,
                countries = countries.toSortedSet(Comparator.comparing { c -> c.country }),
                actinMolecularEvents = entry.value.map { ewt -> ewt.event }.toSortedSet(),
                sourceMolecularEvents = entry.value.flatMap { ewt -> ewt.trial.molecularMatches.map { it.sourceEvent } }.toSortedSet(),
                applicableCancerTypes = entry.value.flatMap { ewt -> ewt.trial.applicableCancerTypes }
                    .toSortedSet(Comparator.comparing { cancerType -> cancerType.matchedCancerType }),
                url = trial.url
            )
        }
            .toSortedSet(compareBy<ExternalTrialSummary> { it.actinMolecularEvents.joinToString() }
                .thenBy { it.sourceMolecularEvents.joinToString() }
                .thenBy { it.applicableCancerTypes.joinToString { cancerType -> cancerType.matchedCancerType } }
                .thenBy { it.nctId })
    }
}