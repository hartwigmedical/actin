package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.util.MapFunctions

data class MolecularFilteredExternalTrials(
    private val original: Set<ExternalTrialSummary>,
    val filtered: Set<ExternalTrialSummary>
) {
    fun originalMinusFilteredSize() = original.size - filtered.size
    fun isNotEmpty() = original.isNotEmpty()
    fun originalMinusFiltered() = original - filtered
}

data class SummarizedExternalTrials(
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

    fun cohortsWithSlotsAvailable(): List<InterpretedCohort> {
        return EligibleActinTrialsGenerator.filterCohortsAvailable(cohorts, true)
    }

    fun cohortsWithSlotsAvailableAndNotIgnore(): List<InterpretedCohort> {
        return EligibleActinTrialsGenerator.filterCohortsAvailable(cohorts.filter { !it.ignore }, true)
    }

    fun allEvidenceSources(): Set<String> {
        return patientRecord.molecularHistory.molecularTests.map { it.evidenceSource }.toSet()
    }

    fun summarizeExternalTrials(
        evaluated: List<InterpretedCohort>,
    ): SummarizedExternalTrials {
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
}