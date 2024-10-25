package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.Cohort

data class ExternalTrialSummary(
    val localTrials: Map<String, Iterable<ExternalTrial>>,
    val localTrialsFiltered: Int,
    val nonLocalTrials: Map<String, Iterable<ExternalTrial>>,
    val nonLocalTrialsFiltered: Int
)

private val CHILDREN_HOSPITALS =
    setOf("PMC", "WKZ", "EKZ", "JKZ", "BKZ", "WAKZ", "Sophia Kinderziekenhuis", "Amalia Kinderziekenhuis", "MosaKids Kinderziekenhuis")

class ExternalTrialSummarizer(private val homeCountry: CountryName) {

    fun summarize(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>,
        trialMatches: List<TrialMatch>,
        cohorts: List<Cohort>
    ): ExternalTrialSummary {
        return filterMolecularCriteriaAlreadyPresent(
            filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches),
            cohorts
        )
    }

    fun filterAndGroupExternalTrialsByNctIdAndEvents(
        externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>, trialMatches: List<TrialMatch>
    ): Map<String, List<ExternalTrial>> {
        val localTrialNctIds = trialMatches.mapNotNull { it.identification.nctId }.toSet()
        return externalTrialsPerEvent.flatMap { (event, trials) ->
            trials.filter { it.nctId !in localTrialNctIds }.filter { trial ->
                val hospitalsInHomeCountry = findHospitalsInHomeCountry(trial)
                hospitalsInHomeCountry.isEmpty() || !hospitalsInHomeCountry.all { hospital -> hospital in CHILDREN_HOSPITALS }
            }.map { event to it }
        }
            .groupBy { (_, trial) -> trial.nctId }
            .map { (_, eventAndTrialPairs) ->
                val (events, trials) = eventAndTrialPairs.unzip()
                events.toSet().joinToString(",\n") to trials.first()
            }
            .groupBy({ it.first }, { it.second })
    }

    fun filterMolecularCriteriaAlreadyPresent(
        externalEligibleTrials: Map<String, Iterable<ExternalTrial>>,
        hospitalLocalEvaluatedCohorts: List<Cohort>
    ): ExternalTrialSummary {

        val hospitalTrialMolecularEvents = hospitalLocalEvaluatedCohorts.flatMap { e -> e.molecularEvents }.toSet()
        val (localTrials, localTrialsFiltered) = filteredMolecularEvents(
            hospitalTrialMolecularEvents,
            EligibleExternalTrialGeneratorFunctions.localTrials(externalEligibleTrials, homeCountry)
        )
        val (nonLocalTrials, nonLocalTrialsFiltered) = filteredMolecularEvents(
            hospitalTrialMolecularEvents + localTrials.keys.flatMap { splitMolecularEvents(it) },
            EligibleExternalTrialGeneratorFunctions.nonLocalTrials(externalEligibleTrials, homeCountry)
        )
        return ExternalTrialSummary(localTrials, localTrialsFiltered, nonLocalTrials, nonLocalTrialsFiltered)
    }

    private fun filteredMolecularEvents(
        molecularTargetsAlreadyIncluded: Set<String>,
        trials: Map<String, Iterable<ExternalTrial>>
    ): Pair<Map<String, Iterable<ExternalTrial>>, Int> {
        val filtered = trials.filterNot {
            splitMolecularEvents(it.key).all { mt -> molecularTargetsAlreadyIncluded.contains(mt) }
        }
        return filtered to uniqueTrialCount(trials) - uniqueTrialCount(filtered)
    }

    private fun uniqueTrialCount(trials: Map<String, Iterable<ExternalTrial>>) =
        trials.flatMap { it.value }.toSet().size

    private fun splitMolecularEvents(molecularEvents: String) =
        molecularEvents.split(",").map { it.trim() }.toSet()

    private fun findHospitalsInHomeCountry(trial: ExternalTrial): Set<String> {
        val homeCountries = trial.countries.filter { it.name == homeCountry }
        if (homeCountries.size > 1) throw IllegalStateException(
            "Country ${homeCountry.display()} is configured multiple times for trial ${trial.nctId}. " +
                    "This should not be possible and indicates an issue in the SERVE data export"
        )
        return homeCountries.firstOrNull()?.hospitalsPerCity?.values?.flatten()?.toSet() ?: emptySet()
    }
}