package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.Driver

class EvaluatedCohortsInterpreter(
    private val eligibleOpenTrialsByInclusionEvent: Map<String, List<String>>,
    private val inclusionEventsOfOpenTrials: Set<String>
) {

    fun trialsForDriver(driver: Driver): List<String> {
        return eligibleOpenTrialsByInclusionEvent[driver.event] ?: emptyList()
    }

    fun driverIsActionable(driver: Driver): Boolean {
        return (driver.evidence.externalEligibleTrials.isNotEmpty() || inclusionEventsOfOpenTrials.contains(driver.event)
                || driver.evidence.approvedTreatments().isNotEmpty())
    }

    companion object {

        fun fromEvaluatedCohorts(evaluatedCohorts: List<EvaluatedCohort>): EvaluatedCohortsInterpreter {
            val openCohorts = evaluatedCohorts.filter(EvaluatedCohort::isOpen)

            val eligibleOpenTrialsByInclusionEvent = openCohorts
                .filter(EvaluatedCohort::isPotentiallyEligible)
                .flatMap { cohort -> cohort.molecularEvents.map { it to cohort.acronym } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, acronyms) -> acronyms.sorted().distinct() }

            val inclusionEventsOfNonBlacklistedOpenTrials = openCohorts
                .flatMap(EvaluatedCohort::molecularEvents)
                .toSet()

            return EvaluatedCohortsInterpreter(eligibleOpenTrialsByInclusionEvent, inclusionEventsOfNonBlacklistedOpenTrials)
        }
    }
}