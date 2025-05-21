package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.trial.TrialSource
import java.util.*
import kotlin.Comparator

class GeneralizedTrialProvider(private val trialsProvider: TrialsProvider) {

    fun allTrialsForOncoAct(): List<GeneralizedTrial> {
        return cohortsWithSlotsAvailableAsGeneralizedTrial() + nationalTrialsAsGeneralizedTrial()
    }

    private fun cohortsWithSlotsAvailableAsGeneralizedTrial(): List<GeneralizedTrial> {
        return trialsProvider.eligibleCohortsWithSlotsAvailableAndNotIgnore().map {
            GeneralizedTrial(
                trialId = it.trialId,
                nctId = it.nctId,
                source = sourceFromTrialSource(it.source),
                acronym = it.acronym,
                title = it.title,
                isOpen = it.isOpen,
                hasSlots = it.hasSlotsAvailable,
                countries = locationsToCountryDetails(it.locations),
                treatments = emptySet(),
                molecularMatches = emptySet(),
                actinMolecularEvents = it.molecularEvents.toSortedSet(),
                sourceMolecularEvents = emptySet(),
                applicableCancerTypes = emptySet(),
                url = optionalUrl(it.nctId)
            )
        }
    }

    private fun nationalTrialsAsGeneralizedTrial(): List<GeneralizedTrial> {
        val externalTrials = trialsProvider.externalTrials()
        return externalTrialsAsGeneralizedTrial(externalTrials.nationalTrials.filtered)
    }

    companion object {

        fun externalTrialsAsGeneralizedTrial(externalTrials: Set<EventWithExternalTrial>): List<GeneralizedTrial> {
            return externalTrials.map {
                val trial = it.trial
                GeneralizedTrial(
                    trialId = trial.nctId,
                    nctId = trial.nctId,
                    source = trial.source,
                    acronym = trial.acronym,
                    title = trial.title,
                    isOpen = null,
                    hasSlots = null,
                    countries = trial.countries,
                    treatments = trial.treatments,
                    molecularMatches = trial.molecularMatches,
                    actinMolecularEvents = setOf(it.event),
                    sourceMolecularEvents = emptySet(),
                    applicableCancerTypes = trial.applicableCancerTypes,
                    url = url(trial.nctId)
                )
            }
        }

        fun locationsToCountryDetails(location: Set<String>): SortedSet<CountryDetails> {
            return location.map { l -> CountryDetails(Country.NETHERLANDS, mapOf(Pair("", setOf(Hospital(l, false))))) }
                .toSortedSet(Comparator.comparing { c -> c.country })
        }

        fun sourceFromTrialSource(source: TrialSource?): String {
            return source?.name ?: "ACTIN"
        }

        fun optionalUrl(nctId: String?): String? {
            return if (nctId != null) url(nctId) else null
        }

        fun url(nctId: String): String {
            return "https://clinicaltrials.gov/study/$nctId"
        }
    }
}