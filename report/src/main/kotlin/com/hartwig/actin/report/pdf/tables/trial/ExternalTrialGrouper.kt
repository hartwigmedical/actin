package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.InterpretedCohort
import java.util.SortedSet

data class SingularExternalTrial(
    val nctId: String,
    val title: String,
    val actinMolecularEvents: SortedSet<String>,
    val ckbMolecularEvents: SortedSet<String>,
    val countries: SortedSet<CountryName>,
    val cities: SortedSet<String>,
    val hospitals: SortedSet<String>
)

data class EventWithExternalTrial(val event: String, val trial: ExternalTrial)

private val CHILDREN_HOSPITALS =
    setOf("PMC", "WKZ", "EKZ", "JKZ", "BKZ", "WAKZ", "Sophia Kinderziekenhuis", "Amalia Kinderziekenhuis", "MosaKids Kinderziekenhuis")

object ExternalTrialGroupAndFilter {

    fun group(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>): Set<SingularExternalTrial> {
        return externalTrialsPerEvent.flatMap {
            it.value.map { t -> EventWithExternalTrial(it.key, t) }.groupBy { t -> t.trial.nctId }
                .map { e ->
                    SingularExternalTrial(
                        e.key,
                        e.value.first().trial.title,
                        e.value.map { f -> f.event }.toSortedSet(),
                        e.value.map { g -> g.trial.sourceEvent }.toSortedSet(),
                        e.value.flatMap { h -> h.trial.countries.map { i -> i.name } }.toSortedSet(),
                        e.value.flatMap { h -> h.trial.countries.flatMap { i -> i.hospitalsPerCity.keys } }.toSortedSet(),
                        e.value.flatMap { j -> j.trial.countries.flatMap { k -> k.hospitalsPerCity.values.flatten() } }.toSortedSet()
                    )
                }
        }.toSet()
    }

    fun filterInternalTrials(externalTrials: Set<SingularExternalTrial>, internalTrials: Set<TrialMatch>): Set<SingularExternalTrial> {
        val internalIds = internalTrials.map { it.identification.nctId }.toSet()
        return externalTrials.filter { it.nctId !in internalIds }.toSet()
    }

    fun filterInHomeCountry(externalTrials: Set<SingularExternalTrial>, country: Country): Set<SingularExternalTrial> {
        return externalTrials.filter { country.name in it.countries }.toSet()
    }

    fun filterChildrensHospitals(externalTrials: Set<SingularExternalTrial>): Set<SingularExternalTrial> {
        return externalTrials.filter {
            it.cities.any { c -> c in CHILDREN_HOSPITALS }
        }.toSet()
    }

    fun filterMolecularCriteriaAlreadyPresent(
        externalTrials: Set<SingularExternalTrial>,
        hospitalLocalEvaluatedCohorts: List<InterpretedCohort>
    ): Pair<Set<SingularExternalTrial>, Int> {
        val hospitalTrialMolecularEvents = hospitalLocalEvaluatedCohorts.flatMap { e -> e.molecularEvents }.toSet()
        val filtered = externalTrials.filter { it.actinMolecularEvents.subtract(hospitalTrialMolecularEvents).isEmpty() }.toSet()
        return filtered to (externalTrials.size - filtered.size)
    }
}