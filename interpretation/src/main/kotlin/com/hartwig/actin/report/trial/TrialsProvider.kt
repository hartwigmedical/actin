package com.hartwig.actin.report.trial

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.interpretation.ActionableAndEvidenceFactory
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory

const val YOUNG_ADULT_CUT_OFF = 40

data class ActionableWithExternalTrial(val actionable: Actionable, val trial: ExternalTrial)

class MolecularFilteredExternalTrials(val original: Set<ActionableWithExternalTrial>, val filtered: Set<ActionableWithExternalTrial>) {

    fun isNotEmpty() = original.isNotEmpty()

    fun originalMinusFiltered() = original - filtered
}

class ExternalTrials(val nationalTrials: MolecularFilteredExternalTrials, val internationalTrials: MolecularFilteredExternalTrials) {

    fun allFiltered(): Set<ActionableWithExternalTrial> {
        return nationalTrials.filtered + internationalTrials.filtered
    }

    fun excludedNationalTrials() = nationalTrials.originalMinusFiltered()

    fun excludedInternationalTrials() = internationalTrials.originalMinusFiltered()
}

class TrialsProvider(
    private val externalTrials: Set<ActionableWithExternalTrial>,
    private val evaluableCohorts: List<InterpretedCohort>,
    private val nonEvaluableCohorts: List<InterpretedCohort>,
    private val internalTrialIds: Set<String>,
    private val patientIsYoungAdult: Boolean,
    private val isLungCancer: Boolean,
    private val countryOfReference: Country,
    private val retainOriginalExternalTrials: Boolean
) {
    fun evaluableCohorts(): List<InterpretedCohort> {
        return evaluableCohorts
    }

    fun evaluableCohortsAndNotIgnore(): List<InterpretedCohort> {
        return evaluableCohorts.filter { !it.ignore }
    }

    fun nonEvaluableCohorts(): List<InterpretedCohort> {
        return nonEvaluableCohorts
    }

    private fun eligibleCohortsWithSlotsAvailableAndNotIgnore(): List<InterpretedCohort> {
        return filterCohortsOpenAndEligible(evaluableCohorts.filter { !it.ignore && it.hasSlotsAvailable })
    }

    fun externalTrialsUnfiltered(): ExternalTrials {
        return externalTrials(setOf(), listOf(), patientIsYoungAdult = false, isLungCancer = false)
    }

    fun externalTrials(): ExternalTrials {
        return externalTrials(internalTrialIds, eligibleCohortsWithSlotsAvailableAndNotIgnore(), patientIsYoungAdult, isLungCancer)
    }

    private fun externalTrials(
        internalTrialIds: Set<String>,
        internalEvaluatedCohorts: List<InterpretedCohort>,
        patientIsYoungAdult: Boolean,
        isLungCancer: Boolean
    ): ExternalTrials {
        val eligibleExternalTrials = externalTrials.filterInternalTrials(internalTrialIds)

        val (nationalTrials, internationalTrials) = partitionByCountry(eligibleExternalTrials, countryOfReference)

        val filteredNationalTrials =
            nationalTrials.filterExclusivelyInChildrensHospitalsInReferenceCountry(patientIsYoungAdult, countryOfReference)

        val filteredInternationalTrials =
            internationalTrials.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(internalEvaluatedCohorts)
                .filterMolecularCriteriaAlreadyPresentInTrials(filteredNationalTrials)

        return ExternalTrials(
            hideOverlappingTrials(
                nationalTrials,
                if (isLungCancer) emptySet() else filteredNationalTrials,
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
        original: Set<ActionableWithExternalTrial>,
        filtered: Set<ActionableWithExternalTrial>,
        retainOriginalTrials: Boolean
    ): MolecularFilteredExternalTrials {
        return MolecularFilteredExternalTrials(
            original,
            when {
                retainOriginalTrials -> original

                else -> filtered
            }
        )
    }

    companion object {
        fun create(
            patientRecord: PatientRecord,
            treatmentMatch: TreatmentMatch,
            countryOfReference: Country,
            doidModel: DoidModel,
            retainOriginalExternalTrials: Boolean,
            filterOnSoCExhaustionAndTumorType: Boolean,
            filter: Function1<Actionable, Boolean> = { true }
        ): TrialsProvider {
            return create(
                patientRecord,
                treatmentMatch.trialMatches,
                countryOfReference,
                (treatmentMatch.referenceDate.year - patientRecord.patient.birthYear) < YOUNG_ADULT_CUT_OFF,
                DoidEvaluationFunctions.isOfDoidType(doidModel, patientRecord.tumor.doids, DoidConstants.LUNG_CANCER_DOID),
                retainOriginalExternalTrials,
                filterOnSoCExhaustionAndTumorType,
                filter
            )
        }

        fun create(
            patientRecord: PatientRecord,
            trialMatches: List<TrialMatch>,
            countryOfReference: Country,
            patientIsYoungAdult: Boolean,
            isLungCancer: Boolean,
            retainOriginalExternalTrials: Boolean,
            filterOnSOCExhaustionAndTumorType: Boolean,
            filter: Function1<Actionable, Boolean> = { true }
        ): TrialsProvider {
            val evaluableCohorts = InterpretedCohortFactory.createEvaluableCohorts(
                trialMatches,
                filterOnSOCExhaustionAndTumorType
            )
            val nonEvaluableCohorts = InterpretedCohortFactory.createNonEvaluableCohorts(trialMatches)
            val internalTrialIds = trialMatches.mapNotNull { it.identification.nctId }.toSet()
            return TrialsProvider(
                externalEligibleTrials(patientRecord, filter),
                evaluableCohorts,
                nonEvaluableCohorts,
                internalTrialIds,
                patientIsYoungAdult,
                isLungCancer,
                countryOfReference,
                retainOriginalExternalTrials
            )
        }

        private fun externalEligibleTrials(
            patientRecord: PatientRecord,
            filter: Function1<Actionable, Boolean>
        ): Set<ActionableWithExternalTrial> {
            return patientRecord.molecularTests.flatMap { test ->
                ActionableAndEvidenceFactory.createTrialEvidences(test, filter).flatMap {
                    it.second.map { trial -> ActionableWithExternalTrial(it.first, trial) }
                }
            }.toSet()
        }

        fun filterCohortsOpenAndEligible(cohorts: List<InterpretedCohort>): List<InterpretedCohort> {
            return cohorts.filter { it.isPotentiallyEligible && it.isOpen && !it.isMissingMolecularResultForEvaluation }
        }

        private fun countryNames(it: ActionableWithExternalTrial) = it.trial.countries.map { c -> c.country }

        private fun partitionByCountry(
            trials: Set<ActionableWithExternalTrial>,
            country: Country
        ): Pair<Set<ActionableWithExternalTrial>, Set<ActionableWithExternalTrial>> {
            val (a, b) = trials.partition { country in countryNames(it).toSet() }
            return a.toSet() to b.toSet()
        }
    }
}

fun Set<ActionableWithExternalTrial>.filterInternalTrials(internalTrialIds: Set<String>): Set<ActionableWithExternalTrial> {
    return this.filter { it.trial.nctId !in internalTrialIds }.toSet()
}

fun Set<ActionableWithExternalTrial>.filterMolecularCriteriaAlreadyPresentInInterpretedCohorts(
    internalEvaluatedCohorts: List<InterpretedCohort>
): Set<ActionableWithExternalTrial> {
    return filterMolecularCriteriaAlreadyPresent(internalEvaluatedCohorts.flatMap { it.molecularInclusionEvents }.toSet())
}

fun Set<ActionableWithExternalTrial>.filterMolecularCriteriaAlreadyPresentInTrials(trials: Set<ActionableWithExternalTrial>):
        Set<ActionableWithExternalTrial> {
    return filterMolecularCriteriaAlreadyPresent(trials.map { it.actionable.event }.toSet())
}

private fun hospitalsForCountry(trial: ExternalTrial, country: Country) =
    trial.countries.firstOrNull { it.country == country }?.hospitalsPerCity?.flatMap { it.value }?.toSet()
        ?: throw IllegalArgumentException("Country not found")


fun Set<ActionableWithExternalTrial>.filterExclusivelyInChildrensHospitalsInReferenceCountry(
    patientIsYoungAdult: Boolean,
    countryOfReference: Country
): Set<ActionableWithExternalTrial> {
    return this.filter { ewt ->
        val allHospitalsAreChildrensInReferenceCountry =
            hospitalsForCountry(ewt.trial, countryOfReference).all { it.isChildrensHospital == true }
        !allHospitalsAreChildrensInReferenceCountry || patientIsYoungAdult
    }.toSet()
}

private fun Set<ActionableWithExternalTrial>.filterMolecularCriteriaAlreadyPresent(presentEvents: Set<String>): Set<ActionableWithExternalTrial> {
    return filter {
        !presentEvents.contains(it.actionable.event)
    }.toSet()
}