package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.evidence.CancerType
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.report.trial.GeneralizedTrialProvider.Companion.url
import java.util.SortedSet

data class ExternalTrialSummary(
    val nctId: String,
    val title: String,
    val countries: SortedSet<CountryDetails>,
    val actinMolecularEvents: SortedSet<String>,
    val sourceMolecularEvents: SortedSet<String>,
    val applicableCancerTypes: SortedSet<CancerType>,
) {
    val url: String = url(nctId)
}

object ExternalTrialSummarizer {

    fun summarize(externalTrialsPerEvent: Iterable<EventWithExternalTrial>): Set<ExternalTrialSummary> {
        return externalTrialsPerEvent.groupBy { ewt -> ewt.trial.nctId }.map { entry ->
            val countries = entry.value.flatMap { ewt -> ewt.trial.countries }
            val trial = entry.value.first().trial
            ExternalTrialSummary(
                nctId = entry.key,
                title = trial.title(),
                countries = countries.toSortedSet(Comparator.comparing { c -> c.country }),
                actinMolecularEvents = entry.value.map { ewt -> ewt.event }.toSortedSet(),
                sourceMolecularEvents = entry.value.flatMap { ewt -> ewt.trial.molecularMatches.map { it.sourceEvent } }.toSortedSet(),
                applicableCancerTypes = entry.value.flatMap { ewt -> ewt.trial.applicableCancerTypes }
                    .toSortedSet(Comparator.comparing { cancerType -> cancerType.matchedCancerType })
            )
        }
            .toSortedSet(compareBy<ExternalTrialSummary> { it.actinMolecularEvents.joinToString() }
                .thenBy { it.sourceMolecularEvents.joinToString() }
                .thenBy { it.applicableCancerTypes.joinToString { cancerType -> cancerType.matchedCancerType } }
                .thenBy { it.nctId })
    }
}