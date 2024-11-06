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
    val hospitals: SortedSet<Hospital>
)

data class EventWithExternalTrial(val event: String, val trial: ExternalTrial)

data class Hospital(val name: String, val isChildrensHospital: Boolean = false)

private val CHILDREN_HOSPITALS =
    setOf("PMC", "WKZ", "EKZ", "JKZ", "BKZ", "WAKZ", "Sophia Kinderziekenhuis", "Amalia Kinderziekenhuis", "MosaKids Kinderziekenhuis")


fun Set<ExternalTrialSummary>.filterInternalTrials(internalTrials: Set<TrialMatch>): Set<ExternalTrialSummary> {
    val internalIds = internalTrials.map { it.identification.nctId }.toSet()
    return this.filter { it.nctId !in internalIds }.toSet()
}

fun Set<ExternalTrialSummary>.filterInCountryOfReference(country: CountryName): Set<ExternalTrialSummary> {
    return this.filter { country in countryNames(it).toSet() }.toSet()
}

private fun countryNames(it: ExternalTrialSummary) = it.countries.map { c -> c.name }

fun Set<ExternalTrialSummary>.filterNotInCountryOfReference(country: CountryName): Set<ExternalTrialSummary> {
    return this.filter { country !in countryNames(it) }.toSet()
}

fun Set<ExternalTrialSummary>.filterExclusivelyInChildrensHospitals(): Set<ExternalTrialSummary> {
    return this.filter {
        !it.hospitals.all(Hospital::isChildrensHospital)
    }.toSet()
}

fun Set<ExternalTrialSummary>.filterMolecularCriteriaAlreadyPresent(internalEvaluatedCohorts: List<InterpretedCohort>): Set<ExternalTrialSummary> {
    return filter {
        it.actinMolecularEvents.subtract(internalEvaluatedCohorts.flatMap { e -> e.molecularEvents }.toSet()).isNotEmpty()
    }.toSet()
}

object ExternalTrialSummarizer {

    fun summarize(externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>): Set<ExternalTrialSummary> {
        return externalTrialsPerEvent.flatMap {
            it.value.map { t -> EventWithExternalTrial(it.key, t) }
        }.groupBy { t -> t.trial.nctId }.map { e ->
            val countries = e.value.flatMap { ewe -> ewe.trial.countries }
            val hospitals = countries.flatMap { c -> c.hospitalsPerCity.entries.map { hpc -> c to hpc } }
            val trial = e.value.first().trial
            ExternalTrialSummary(e.key,
                trial.title,
                trial.url,
                e.value.map { ewe -> ewe.event }.toSortedSet(),
                e.value.map { ewe -> ewe.trial.sourceEvent }.toSortedSet(),
                e.value.map { ewe -> ewe.trial.applicableCancerType }.toSortedSet(Comparator.comparing { c -> c.cancerType }),
                countries.toSortedSet(Comparator.comparing { c -> c.name }),
                hospitals.map { h -> h.second.key }.toSortedSet(),
                hospitals.map { h -> h.second.value.map { i -> h.first to i } }.flatten()
                    .map { h -> Hospital(h.second, isChildrensHospitalInNetherlands(h)) }.toSortedSet(Comparator.comparing { h -> h.name })
            )
        }
            .toSortedSet(compareBy<ExternalTrialSummary> { it.actinMolecularEvents.joinToString() }.thenBy { it.sourceMolecularEvents.joinToString() }
                .thenBy { it.cancerTypes.joinToString { t -> t.cancerType } }.thenBy { it.nctId })
    }

    private fun isChildrensHospitalInNetherlands(h: Pair<Country, String>) =
        h.second in CHILDREN_HOSPITALS && h.first.name == CountryName.NETHERLANDS
}