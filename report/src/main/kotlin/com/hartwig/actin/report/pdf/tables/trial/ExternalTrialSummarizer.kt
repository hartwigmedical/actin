package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.InterpretedCohort
import java.util.SortedSet

data class ExternalTrialSummary(
    val nctId: String,
    val title: String,
    val url: String,
    val actinMolecularEvents: SortedSet<String>,
    val sourceMolecularEvents: SortedSet<String>,
    val cancerTypes: SortedSet<ApplicableCancerType>,
    val countries: SortedSet<Country>,
    val cities: SortedSet<String>,
    val hospitals: SortedSet<String>
)

data class EventWithExternalTrial(val event: String, val trial: ExternalTrial)

private val CHILDREN_HOSPITALS =
    setOf("PMC", "WKZ", "EKZ", "JKZ", "BKZ", "WAKZ", "Sophia Kinderziekenhuis", "Amalia Kinderziekenhuis", "MosaKids Kinderziekenhuis")


fun Set<ExternalTrialSummary>.filterInternalTrials(internalTrials: Set<TrialMatch>): Set<ExternalTrialSummary> {
    val internalIds = internalTrials.map { it.identification.nctId }.toSet()
    return this.filter { it.nctId !in internalIds }.toSet()
}

fun Set<ExternalTrialSummary>.filterInHomeCountry(country: CountryName): Set<ExternalTrialSummary> {
    return this.filter { country in countryNames(it).toSet() }.toSet()
}

private fun countryNames(it: ExternalTrialSummary) = it.countries.map { c -> c.name }

fun Set<ExternalTrialSummary>.filterNotInHomeCountry(country: CountryName): Set<ExternalTrialSummary> {
    return this.filter { country !in countryNames(it) }.toSet()
}

fun Set<ExternalTrialSummary>.filterChildrensHospitals(): Set<ExternalTrialSummary> {
    return this.filter {
        it.cities.all { c -> c !in CHILDREN_HOSPITALS }
    }.toSet()
}

fun Set<ExternalTrialSummary>.filterMolecularCriteriaAlreadyPresent(hospitalLocalEvaluatedCohorts: List<InterpretedCohort>): Pair<Set<ExternalTrialSummary>, Int> {
    val hospitalTrialMolecularEvents = hospitalLocalEvaluatedCohorts.flatMap { e -> e.molecularEvents }.toSet()
    val filtered = this.filter { it.actinMolecularEvents.subtract(hospitalTrialMolecularEvents).isNotEmpty() }.toSet()
    return filtered to (this.size - filtered.size)
}

object ExternalTrialSummarizer {

    fun summarize(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>): Set<ExternalTrialSummary> {
        val flattened = externalTrialsPerEvent.flatMap {
            it.value.map { t -> EventWithExternalTrial(it.key, t) }
        }
        return flattened.groupBy { t -> t.trial.nctId }.map { e ->
            val countries = e.value.flatMap { ewe -> ewe.trial.countries }
            val hospitals = countries.flatMap { ewe -> ewe.hospitalsPerCity.entries }
            val trial = e.value.first().trial
            ExternalTrialSummary(
                e.key,
                trial.title,
                trial.url,
                e.value.map { ewe -> ewe.event }.toSortedSet(),
                e.value.map { ewe -> ewe.trial.sourceEvent }.toSortedSet(),
                e.value.map { ewe -> ewe.trial.applicableCancerType }.toSortedSet(Comparator.comparing { c -> c.cancerType }),
                countries.toSortedSet(Comparator.comparing { c -> c.name }),
                hospitals.map { h -> h.key }.toSortedSet(),
                hospitals.map { h -> h.value }.flatten().toSortedSet()
            )
        }.toSet()
    }
}