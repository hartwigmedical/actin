package com.hartwig.actin.report.trial

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import java.time.LocalDate

data class EventWithExternalTrial(val event: String, val trial: ExternalTrial)

class MolecularFilteredExternalTrials(private val original: Set<EventWithExternalTrial>, val filtered: Set<EventWithExternalTrial>) {

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
        return filterCohortsAvailable(cohorts.filter { !it.ignore })
    }

    private fun externalEligibleTrials(): Set<EventWithExternalTrial> {
        return patientRecord.molecularHistory.molecularTests.flatMap { t ->
            AggregatedEvidenceFactory.create(t).eligibleTrialsPerEvent.flatMap {
                it.value.map { trial -> EventWithExternalTrial(it.key, trial) }
            }
        }.toSet()
    }

    fun externalTrials(): ExternalTrials {
        val evaluated = eligibleCohortsWithSlotsAvailableAndNotIgnore()

        val eligibleExternalTrials = externalEligibleTrials().filterInternalTrials(treatmentMatch.trialMatches)

        val (nationalTrials, internationalTrials) = partitionByCountry(eligibleExternalTrials, countryOfReference)

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

        val internationalTrialsNotOverlappingHospitalOrNational = hideOverlappingTrials(
            internationalTrials,
            internationalTrials.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(evaluated)
                .filterMolecularCriteriaAlreadyPresentInTrials(nationalTrials),
            enableExtendedMode
        )
        return ExternalTrials(nationalTrialsNotOverlappingHospital, internationalTrialsNotOverlappingHospitalOrNational)
    }

    private fun hideOverlappingTrials(
        original: Set<EventWithExternalTrial>,
        filtered: Set<EventWithExternalTrial>,
        enableExtendedMode: Boolean
    ): MolecularFilteredExternalTrials {
        return MolecularFilteredExternalTrials(
            original.toSet(),
            (if (enableExtendedMode) original else filtered).toSet()
        )
    }

    companion object {
        fun filterCohortsAvailable(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filter {
                it.isPotentiallyEligible && it.isOpen && !it.isMissingMolecularResultForEvaluation
            }
        }

        private fun countryNames(it: EventWithExternalTrial) = it.trial.countries.map { c -> c.country }

        fun partitionByCountry(
            trials: Set<EventWithExternalTrial>,
            country: Country
        ): Pair<Set<EventWithExternalTrial>, Set<EventWithExternalTrial>> {
            val (a, b) = trials.partition { country in countryNames(it).toSet() }
            return a.toSet() to b.toSet()
        }
    }
}

fun Set<EventWithExternalTrial>.filterInternalTrials(internalTrials: List<TrialMatch>): Set<EventWithExternalTrial> {
    val internalIds = internalTrials.map { it.identification.nctId }.toSet()
    return this.filter { it.trial.nctId !in internalIds }.toSet()
}

fun Set<EventWithExternalTrial>.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(
    internalEvaluatedCohorts: List<InterpretedCohort>
): Set<EventWithExternalTrial> {
    return filterMolecularCriteriaAlreadyPresent(internalEvaluatedCohorts.flatMap { it.molecularEvents }.toSet())
}

fun Set<EventWithExternalTrial>.filterMolecularCriteriaAlreadyPresentInTrials(trials: Set<EventWithExternalTrial>):
        Set<EventWithExternalTrial> {
    return filterMolecularCriteriaAlreadyPresent(trials.map { it.event }.toSet())
}

private fun hospitalsNamesForCountry(trial: ExternalTrial, country: Country) =
    trial.countries.firstOrNull { it.country == country }?.hospitalsPerCity?.flatMap { it.value }?.toSet()
        ?: throw IllegalArgumentException("Country not found")


fun Set<EventWithExternalTrial>.filterExclusivelyInChildrensHospitalsInReferenceCountry(
    birthYear: Int,
    referenceDate: LocalDate,
    countryOfReference: Country
): Set<EventWithExternalTrial> {
    val isYoungAdult = referenceDate.year - birthYear < 40
    return this.filter { ewt ->
        val allHospitalsAreChildrensInReferenceCountry =
            hospitalsNamesForCountry(ewt.trial, countryOfReference).all { it.isChildrensHospital == true }
        !allHospitalsAreChildrensInReferenceCountry || isYoungAdult
    }.toSet()
}

private fun Set<EventWithExternalTrial>.filterMolecularCriteriaAlreadyPresent(presentEvents: Set<String>): Set<EventWithExternalTrial> {
    return filter {
        !presentEvents.contains(it.event)
    }.toSet()
}


