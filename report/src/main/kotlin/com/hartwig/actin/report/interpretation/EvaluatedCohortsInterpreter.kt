package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.Driver

class EvaluatedCohortsInterpreter(evaluatedCohorts: List<EvaluatedCohort>) {
    private val eligibleOpenTrialsByInclusionEvent: Map<String, List<String>>
    private val inclusionEventsOfNonBlacklistedOpenTrials: Set<String>

    init {
        eligibleOpenTrialsByInclusionEvent = evaluatedCohorts
            .filter(EvaluatedCohort::isPotentiallyEligible)
            .filter(EvaluatedCohort::isOpen)
            .flatMap { cohort -> cohort.molecularEvents.map { it to cohort.acronym } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, acronym) -> acronym.sorted() }
    }

    init {
        inclusionEventsOfNonBlacklistedOpenTrials = evaluatedCohorts
            .filterNot(EvaluatedCohort::isBlacklisted)
            .filter(EvaluatedCohort::isOpen)
            .flatMap(EvaluatedCohort::molecularEvents)
            .toSet()
    }

    fun trialsForDriver(driver: Driver): List<String> {
        return eligibleOpenTrialsByInclusionEvent[driver.event()] ?: emptyList()
    }

    fun driverIsActionable(driver: Driver): Boolean {
        return (driver.evidence().externalEligibleTrials()
            .isNotEmpty() || inclusionEventsOfNonBlacklistedOpenTrials.contains(driver.event())
                || driver.evidence().approvedTreatments().isNotEmpty())
    }
}