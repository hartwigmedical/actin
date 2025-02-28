package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.approved

data class TrialAcronymAndLocations(val trialAcronym: String, val locations: List<String>)

class InterpretedCohortsSummarizer(
    private val eligibleOpenTrialsByInclusionEvent: Map<String, List<TrialAcronymAndLocations>>,
    private val inclusionEventsOfOpenTrials: Set<String>
) {

    fun trialsForDriver(driver: Driver): List<TrialAcronymAndLocations> {
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
                .flatMap { cohort -> cohort.molecularEvents.map { it to TrialAcronymAndLocations(cohort.acronym, cohort.locations) } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, acronymsAndLocations) -> acronymsAndLocations.sortedBy { it.trialAcronym }.distinct() }

            val inclusionEventsOfNonIgnoredOpenTrials = openCohorts
                .flatMap(InterpretedCohort::molecularEvents)
                .toSet()

            return InterpretedCohortsSummarizer(eligibleOpenTrialsByInclusionEvent, inclusionEventsOfNonIgnoredOpenTrials)
        }
    }
}