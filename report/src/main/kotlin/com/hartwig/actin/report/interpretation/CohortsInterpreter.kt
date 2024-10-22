package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidenceCategories.approved

class CohortsInterpreter(
    private val eligibleOpenTrialsByInclusionEvent: Map<String, List<String>>,
    private val inclusionEventsOfOpenTrials: Set<String>
) {

    fun trialsForDriver(driver: Driver): List<String> {
        return eligibleOpenTrialsByInclusionEvent[driver.event] ?: emptyList()
    }

    fun driverIsActionable(driver: Driver): Boolean {
        return (driver.evidence.externalEligibleTrials.isNotEmpty() || inclusionEventsOfOpenTrials.contains(driver.event)
                || approved(driver.evidence.treatmentEvidence).isNotEmpty())
    }

    companion object {

        fun fromCohorts(cohorts: List<Cohort>): CohortsInterpreter {
            val openCohorts = cohorts.filter(Cohort::isOpen)

            val eligibleOpenTrialsByInclusionEvent = openCohorts
                .filter(Cohort::isPotentiallyEligible)
                .flatMap { cohort -> cohort.molecularEvents.map { it to cohort.acronym } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, acronyms) -> acronyms.sorted().distinct() }

            val inclusionEventsOfNonIgnoredOpenTrials = openCohorts
                .flatMap(Cohort::molecularEvents)
                .toSet()

            return CohortsInterpreter(eligibleOpenTrialsByInclusionEvent, inclusionEventsOfNonIgnoredOpenTrials)
        }
    }
}