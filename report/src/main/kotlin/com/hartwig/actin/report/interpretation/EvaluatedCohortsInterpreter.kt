package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidenceCategories.approved

class EvaluatedCohortsInterpreter(
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

        fun fromEvaluatedCohorts(evaluatedCohorts: List<EvaluatedCohort>): EvaluatedCohortsInterpreter {
            val openCohorts = evaluatedCohorts.filter(EvaluatedCohort::isOpen)

            val eligibleOpenTrialsByInclusionEvent = openCohorts
                .filter(EvaluatedCohort::isPotentiallyEligible)
                .flatMap { cohort -> cohort.molecularEvents.map { it to cohort.acronym } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, acronyms) -> acronyms.sorted().distinct() }

            val inclusionEventsOfNonIgnoredOpenTrials = openCohorts
                .flatMap(EvaluatedCohort::molecularEvents)
                .toSet()

            return EvaluatedCohortsInterpreter(eligibleOpenTrialsByInclusionEvent, inclusionEventsOfNonIgnoredOpenTrials)
        }
    }
}