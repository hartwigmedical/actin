package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.InterpretedCohort
import java.time.LocalDate
import java.util.SortedSet

data class ExternalTrialSummary(
    val nctId: String,
    val title: String,
    val url: String,
    val actinMolecularEvents: SortedSet<String>,
    val sourceMolecularEvents: SortedSet<String>,
    val cancerTypes: SortedSet<CancerType>,
    val countries: SortedSet<CountryDetails>
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

fun Set<ExternalTrialSummary>.filterInCountryOfReference(country: Country): Set<ExternalTrialSummary> {
    return this.filter { country in countryNames(it).toSet() }.toSet()
}

fun Set<ExternalTrialSummary>.filterNotInCountryOfReference(country: Country): Set<ExternalTrialSummary> {
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
            it.value.map { t -> EventWithExternalTrial(it.key, t) }
        }.groupBy { t -> t.trial.nctId }.map { e ->
            val countries = e.value.flatMap { ewe -> ewe.trial.countries }
            val trial = e.value.first().trial
            ExternalTrialSummary(
                e.key,
                trial.title,
                trial.url,
                e.value.map { ewe -> ewe.event }.toSortedSet(),
                e.value.map { ewe -> ewe.trial.molecularMatches.first().sourceEvent }.toSortedSet(),
                e.value.map { ewe -> ewe.trial.applicableCancerTypes.first() }
                    .toSortedSet(Comparator.comparing { c -> c.matchedCancerType }),
                countries.toSortedSet(Comparator.comparing { c -> c.country })
            )
        }
            .toSortedSet(compareBy<ExternalTrialSummary> { it.actinMolecularEvents.joinToString() }.thenBy { it.sourceMolecularEvents.joinToString() }
                .thenBy { it.cancerTypes.joinToString { t -> t.matchedCancerType } }.thenBy { it.nctId })
    }
}