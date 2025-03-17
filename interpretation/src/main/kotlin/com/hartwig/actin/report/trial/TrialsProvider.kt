package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.util.MapFunctions
import java.util.*
import java.util.Collections.emptySortedSet

class MolecularFilteredExternalTrials(
    private val original: Set<ExternalTrialSummary>,
    val filtered: Set<ExternalTrialSummary>
) {
    fun originalMinusFilteredSize() = original.size - filtered.size
    fun isNotEmpty() = original.isNotEmpty()
    fun originalMinusFiltered() = original - filtered
}

class SummarizedExternalTrials(
    val nationalTrials: MolecularFilteredExternalTrials,
    val internationalTrials: MolecularFilteredExternalTrials
) {
    fun allFiltered(): Set<ExternalTrialSummary> {
        return nationalTrials.filtered + internationalTrials.filtered
    }
}

class TrialsProvider(
    private val patientRecord: PatientRecord,
    private val treatmentMatch: TreatmentMatch,
    private val countryOfReference: Country,
    private val enableExtendedMode: Boolean,
    filterOnSOCExhaustionAndTumorType: Boolean
) {
    private val cohorts: List<InterpretedCohort> = InterpretedCohortFactory.createEvaluableCohorts(
        treatmentMatch,
        filterOnSOCExhaustionAndTumorType
    )
    private val nonEvaluableCohorts = InterpretedCohortFactory.createNonEvaluableCohorts(treatmentMatch)

    fun allEvaluableCohorts(): List<InterpretedCohort> {
        return cohorts
    }

    fun allNonEvaluableCohorts(): List<InterpretedCohort> {
        return nonEvaluableCohorts
    }

    fun cohortsWithSlotsAvailableAndNotIgnore(): List<InterpretedCohort> {
        return filterCohortsAvailable(cohorts.filter { !it.ignore }, true)
    }

    private fun cohortsWithSlotsAvailableAsGeneralizedTrial(): List<GeneralizedTrial> {
        return cohortsWithSlotsAvailableAndNotIgnore().map {
            GeneralizedTrial(
                it.trialId,
                it.nctId,
                sourceFromTrailSource(it.source),
                it.acronym,
                it.title,
                it.isOpen,
                it.hasSlotsAvailable,
                locationsToCountryDetails(it.locations),
                emptySortedSet(),
                it.molecularEvents.toSortedSet(),
                emptySortedSet(),
                emptySortedSet(),
                ""
            )
        }
    }

    private fun summarizedNationalTrialsAsGeneralizedTrial(): List<GeneralizedTrial> {
        val summarizedExternalTrials = summarizeExternalTrials()
        return summarizedExternalTrials.nationalTrials.filtered.map {
            GeneralizedTrial(
                it.nctId,
                it.nctId,
                "CKB",
                null,
                it.title,
                null,
                null,
                it.countries,
                emptySortedSet(),
                it.actinMolecularEvents,
                it.sourceMolecularEvents,
                it.applicableCancerTypes,
                it.url
            )
        }
    }

    fun allTrialsForOncoAct(): List<GeneralizedTrial> {
        return cohortsWithSlotsAvailableAsGeneralizedTrial() + summarizedNationalTrialsAsGeneralizedTrial()
    }

    fun allEvidenceSources(): Set<String> {
        return patientRecord.molecularHistory.molecularTests.map { it.evidenceSource }.toSet()
    }

    fun summarizeExternalTrials(): SummarizedExternalTrials {
        val evaluated = cohortsWithSlotsAvailableAndNotIgnore()
        val externalEligibleTrials =
            MapFunctions.mergeMapsOfSets(patientRecord.molecularHistory.molecularTests.map {
                AggregatedEvidenceFactory.create(it).eligibleTrialsPerEvent
            })

        val externalEligibleTrialsFiltered = ExternalTrialSummarizer.summarize(externalEligibleTrials)
            .filterInternalTrials(treatmentMatch.trialMatches.toSet())

        val nationalTrials = externalEligibleTrialsFiltered.filterInCountry(countryOfReference)
        val nationalTrialsNotOverlappingHospital =
            hideOverlappingTrials(
                nationalTrials,
                nationalTrials.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(evaluated)
                    .filterExclusivelyInChildrensHospitalsInReferenceCountry(
                        patientRecord.patient.birthYear,
                        treatmentMatch.referenceDate,
                        countryOfReference
                    ),
                enableExtendedMode
            )

        val internationalTrials = externalEligibleTrialsFiltered.filterNotInCountry(countryOfReference)
        val internationalTrialsNotOverlappingHospitalOrNational = hideOverlappingTrials(
            internationalTrials,
            internationalTrials.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(evaluated)
                .filterMolecularCriteriaAlreadyPresentInTrials(nationalTrials),
            enableExtendedMode
        )
        return SummarizedExternalTrials(nationalTrialsNotOverlappingHospital, internationalTrialsNotOverlappingHospitalOrNational)
    }

    private fun hideOverlappingTrials(
        original: Set<ExternalTrialSummary>,
        filtered: Set<ExternalTrialSummary>,
        enableExtendedMode: Boolean
    ): MolecularFilteredExternalTrials {
        return if (enableExtendedMode) MolecularFilteredExternalTrials(
            original,
            original
        ) else MolecularFilteredExternalTrials(
            original,
            filtered
        )
    }

    companion object {
        fun filterCohortsAvailable(cohorts: List<InterpretedCohort>, slotsAvailable: Boolean): List<InterpretedCohort> {
            return cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && it.hasSlotsAvailable == slotsAvailable && !it.isMissingMolecularResultForEvaluation!!
            }
        }

        fun locationsToCountryDetails(location: List<String>): SortedSet<CountryDetails> {
            return location.map { l -> CountryDetails(Country.NETHERLANDS, mapOf(Pair("", setOf(Hospital(l, false))))) }
                .toSortedSet(Comparator.comparing { c -> c.country })
        }

        fun sourceFromTrailSource(source: TrialSource?): String {
            return source?.name ?: "ACTIN"
        }
    }
}

