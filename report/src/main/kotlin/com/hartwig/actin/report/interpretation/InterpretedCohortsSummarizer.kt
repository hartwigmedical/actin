package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.approved

class InterpretedCohortsSummarizer(
    private val eligibleOpenTrialsByInclusionEvent: Map<String, List<String>>,
    private val inclusionEventsOfOpenTrials: Set<String>
) {

    fun trialsForDriver(driver: Driver): List<String> {
        return eligibleOpenTrialsByInclusionEvent[driver.event] ?: emptyList()
    }

    fun driverIsActionable(driver: Driver): Boolean {
        return (driver.evidence.eligibleTrials.isNotEmpty() || inclusionEventsOfOpenTrials.contains(driver.event)
                || approved(driver.evidence.treatmentEvidence).isNotEmpty())
    }

    companion object {
        fun fromCohorts(cohorts: List<InterpretedCohort>): InterpretedCohortsSummarizer {
            val openCohorts = cohorts.filter(InterpretedCohort::isOpen)

            val eligibleOpenTrialsByInclusionEvent = openCohorts
                .filter(InterpretedCohort::isPotentiallyEligible)
                .flatMap { cohort -> cohort.molecularEvents.map { it to cohort.acronym } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, acronyms) -> acronyms.sorted().distinct() }

            val inclusionEventsOfNonIgnoredOpenTrials = openCohorts
                .flatMap(InterpretedCohort::molecularEvents)
                .toSet()

            return InterpretedCohortsSummarizer(eligibleOpenTrialsByInclusionEvent, inclusionEventsOfNonIgnoredOpenTrials)
        }
    }
}