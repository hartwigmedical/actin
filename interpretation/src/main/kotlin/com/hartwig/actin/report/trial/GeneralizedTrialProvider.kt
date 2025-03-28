package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.trial.TrialSource
import java.util.*
import java.util.Collections.emptySortedSet
import kotlin.Comparator

class GeneralizedTrialProvider(private val trialsProvider: TrialsProvider) {

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
                therapyNames = emptySortedSet(),
                actinMolecularEvents = it.molecularEvents.toSortedSet(),
                sourceMolecularEvents = emptySortedSet(),
                applicableCancerTypes = emptySortedSet(),
                url = url(it.nctId)
            )
        }
    }

    private fun summarizedNationalTrialsAsGeneralizedTrial(): List<GeneralizedTrial> {
        val summarizedExternalTrials = trialsProvider.summarizeExternalTrials()
        return summarizedExternalTrials.nationalTrials.filtered.map {
            GeneralizedTrial(
                trialId = it.nctId,
                nctId = it.nctId,
                source = it.source,
                acronym = it.acronym,
                title = it.title,
                isOpen = null,
                hasSlots = null,
                countries = it.countries,
                therapyNames = it.therapyNames,
                actinMolecularEvents = it.actinMolecularEvents,
                sourceMolecularEvents = it.sourceMolecularEvents,
                applicableCancerTypes = it.applicableCancerTypes,
                url = url(it.nctId)
            )
        }
    }

    fun allTrialsForOncoAct(): List<GeneralizedTrial> {
        return cohortsWithSlotsAvailableAsGeneralizedTrial() + summarizedNationalTrialsAsGeneralizedTrial()
    }

    companion object {
        fun locationsToCountryDetails(location: List<String>): SortedSet<CountryDetails> {
            return location.map { l -> CountryDetails(Country.NETHERLANDS, mapOf(Pair("", setOf(Hospital(l, false))))) }
                .toSortedSet(Comparator.comparing { c -> c.country })
        }

        fun sourceFromTrialSource(source: TrialSource?): String {
            return source?.name ?: "ACTIN"
        }

        fun url(nctId: String?): String? {
            return if (nctId != null) "https://clinicaltrials.gov/study/$nctId" else null
        }
    }
}