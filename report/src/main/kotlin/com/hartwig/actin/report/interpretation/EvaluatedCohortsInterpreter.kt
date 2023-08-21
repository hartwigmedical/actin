package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.Driver

class EvaluatedCohortsInterpreter(evaluatedCohorts: List<EvaluatedCohort>) {
    private val eligibleTrialsByInclusionEvent: Map<String, List<String>>

    init {
        eligibleTrialsByInclusionEvent = evaluatedCohorts
            .filter(EvaluatedCohort::isPotentiallyEligible)
            .filter(EvaluatedCohort::isOpen)
            .flatMap { cohort -> cohort.molecularEvents.map { it to cohort.acronym } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, acronym) -> acronym.sorted() }
    }

    fun trialsForDriver(driver: Driver): List<String> {
        return eligibleTrialsByInclusionEvent[driver.event()] ?: emptyList()
    }

    fun driverIsActionable(driver: Driver): Boolean {
        return (driver.evidence().externalEligibleTrials().isNotEmpty() || eligibleTrialsByInclusionEvent.containsKey(driver.event())
                || driver.evidence().approvedTreatments().isNotEmpty())
    }
}