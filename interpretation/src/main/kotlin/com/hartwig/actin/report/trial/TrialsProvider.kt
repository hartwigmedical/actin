package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory

const val YOUNG_ADULT_CUT_OFF = 40

data class EventWithExternalTrial(val event: String, val trial: ExternalTrial, val actionable: Actionable)

class MolecularFilteredExternalTrials(val original: Set<EventWithExternalTrial>, val filtered: Set<EventWithExternalTrial>) {

    fun isNotEmpty() = original.isNotEmpty()

    fun originalMinusFiltered() = original - filtered
}

class ExternalTrials(val nationalTrials: MolecularFilteredExternalTrials, val internationalTrials: MolecularFilteredExternalTrials) {

    fun allFiltered(): Set<EventWithExternalTrial> {
        return nationalTrials.filtered + internationalTrials.filtered
    }

    fun excludedNationalTrials() = nationalTrials.originalMinusFiltered()

    fun excludedInternationalTrials() = internationalTrials.originalMinusFiltered()
}

class TrialsProvider(
    private val externalTrials: Set<EventWithExternalTrial>,
    private val cohorts: List<InterpretedCohort>,
    private val nonEvaluableCohorts: List<InterpretedCohort>,
    private val internalTrialIds: Set<String>,
    private val isYoungAdult: Boolean,
    private val countryOfReference: Country,
    private val retainOriginalExternalTrials: Boolean
) {
    fun evaluableCohorts(): List<InterpretedCohort> {
        return cohorts
    }

    fun evaluableCohortsAndNotIgnore(): List<InterpretedCohort> {
        return cohorts.filter { !it.ignore }
    }

    fun nonEvaluableCohorts(): List<InterpretedCohort> {
        return nonEvaluableCohorts
    }

    private fun eligibleCohortsWithSlotsAvailableAndNotIgnore(): List<InterpretedCohort> {
        return filterCohortsAvailable(cohorts.filter { !it.ignore && it.hasSlotsAvailable })
    }

    fun externalTrialsUnfiltered(): ExternalTrials {
        return externalTrials(setOf(), listOf(), false)
    }

    fun externalTrials(): ExternalTrials {
        return externalTrials(internalTrialIds, eligibleCohortsWithSlotsAvailableAndNotIgnore(), isYoungAdult)
    }

    private fun externalTrials(
        internalTrialIds: Set<String>,
        internalEvaluatedCohorts: List<InterpretedCohort>,
        isYoungAdult: Boolean
    ): ExternalTrials {
        val eligibleExternalTrials = externalTrials.filterInternalTrials(internalTrialIds)

        val (nationalTrials, internationalTrials) = partitionByCountry(eligibleExternalTrials, countryOfReference)

        val filteredNationalTrials =
            nationalTrials.filterExclusivelyInChildrensHospitalsInReferenceCountry(isYoungAdult, countryOfReference)

        val filteredInternationalTrials =
            internationalTrials.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(internalEvaluatedCohorts)
                .filterMolecularCriteriaAlreadyPresentInTrials(filteredNationalTrials)

        return ExternalTrials(
            hideOverlappingTrials(
                nationalTrials,
                filteredNationalTrials,
                retainOriginalExternalTrials
            ),
            hideOverlappingTrials(
                internationalTrials,
                filteredInternationalTrials,
                retainOriginalExternalTrials
            )
        )
    }

    private fun hideOverlappingTrials(
        original: Set<EventWithExternalTrial>,
        filtered: Set<EventWithExternalTrial>,
        retainOriginalTrials: Boolean
    ): MolecularFilteredExternalTrials {
        return MolecularFilteredExternalTrials(
            original,
            if (retainOriginalTrials) original else filtered
        )
    }

    companion object {
        fun create(
            patientRecord: PatientRecord,
            treatmentMatch: TreatmentMatch,
            countryOfReference: Country,
            retainOriginalExternalTrials: Boolean,
            filterOnSOCExhaustionAndTumorType: Boolean,
            filter: Function1<Actionable, Boolean> = { true }
        ): TrialsProvider {
            val isYoungAdult = (treatmentMatch.referenceDate.year - patientRecord.patient.birthYear) < YOUNG_ADULT_CUT_OFF
            val cohorts = InterpretedCohortFactory.createEvaluableCohorts(
                treatmentMatch,
                filterOnSOCExhaustionAndTumorType
            )
            val nonEvaluableCohorts = InterpretedCohortFactory.createNonEvaluableCohorts(treatmentMatch)
            val internalTrialIds = treatmentMatch.trialMatches.mapNotNull { it.identification.nctId }.toSet()
            return TrialsProvider(
                externalEligibleTrials(patientRecord, filter),
                cohorts,
                nonEvaluableCohorts,
                internalTrialIds,
                isYoungAdult,
                countryOfReference,
                retainOriginalExternalTrials
            )
        }

        private fun externalEligibleTrials(
            patientRecord: PatientRecord,
            filter: Function1<Actionable, Boolean>
        ): Set<EventWithExternalTrial> {
            return patientRecord.molecularTests.flatMap { test ->
                AggregatedEvidenceFactory.createTrialEvidences(test, filter).flatMap {
                    it.second.map { trial -> EventWithExternalTrial(it.first.eventName()!!, trial, it.first) }
                }
            }.toSet()
        }

        fun filterCohortsAvailable(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && !it.isMissingMolecularResultForEvaluation
            }
        }

        private fun countryNames(it: EventWithExternalTrial) = it.trial.countries.map { c -> c.country }

        private fun partitionByCountry(
            trials: Set<EventWithExternalTrial>,
            country: Country
        ): Pair<Set<EventWithExternalTrial>, Set<EventWithExternalTrial>> {
            val (a, b) = trials.partition { country in countryNames(it).toSet() }
            return a.toSet() to b.toSet()
        }
    }
}

fun Set<EventWithExternalTrial>.filterInternalTrials(internalTrialIds: Set<String>): Set<EventWithExternalTrial> {
    return this.filter { it.trial.nctId !in internalTrialIds }.toSet()
}

fun Set<EventWithExternalTrial>.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(
    internalEvaluatedCohorts: List<InterpretedCohort>
): Set<EventWithExternalTrial> {
    return filterMolecularCriteriaAlreadyPresent(internalEvaluatedCohorts.flatMap { it.molecularInclusionEvents }.toSet())
}

fun Set<EventWithExternalTrial>.filterMolecularCriteriaAlreadyPresentInTrials(trials: Set<EventWithExternalTrial>):
        Set<EventWithExternalTrial> {
    return filterMolecularCriteriaAlreadyPresent(trials.map { it.event }.toSet())
}

private fun hospitalsForCountry(trial: ExternalTrial, country: Country) =
    trial.countries.firstOrNull { it.country == country }?.hospitalsPerCity?.flatMap { it.value }?.toSet()
        ?: throw IllegalArgumentException("Country not found")


fun Set<EventWithExternalTrial>.filterExclusivelyInChildrensHospitalsInReferenceCountry(
    isYoungAdult: Boolean,
    countryOfReference: Country
): Set<EventWithExternalTrial> {
    return this.filter { ewt ->
        val allHospitalsAreChildrensInReferenceCountry =
            hospitalsForCountry(ewt.trial, countryOfReference).all { it.isChildrensHospital == true }
        !allHospitalsAreChildrensInReferenceCountry || isYoungAdult
    }.toSet()
}

private fun Set<EventWithExternalTrial>.filterMolecularCriteriaAlreadyPresent(presentEvents: Set<String>): Set<EventWithExternalTrial> {
    return filter {
        !presentEvents.contains(it.event)
    }.toSet()
}