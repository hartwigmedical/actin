package com.hartwig.actin.report.interpretation

import com.google.common.collect.Maps
import com.hartwig.actin.molecular.datamodel.driver.Driver
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

class EvaluatedCohortsInterpreter(evaluatedCohorts: List<EvaluatedCohort?>) {
    private val eligibleTrialsByInclusionEvent: Map<String, List<String>>

    init {
        eligibleTrialsByInclusionEvent = evaluatedCohorts.stream()
            .filter { obj: EvaluatedCohort? -> obj!!.isPotentiallyEligible }
            .filter { obj: EvaluatedCohort? -> obj!!.isOpen }
            .flatMap { cohort: EvaluatedCohort? ->
                cohort!!.molecularEvents().stream().map { event: String? -> Maps.immutableEntry(event, cohort.acronym()) }
            }
            .sorted(java.util.Map.Entry.comparingByValue())
            .collect(
                Collectors.groupingBy<Map.Entry<String?, String>, String, Any, List<String>>(
                    Function<Map.Entry<String?, String>, String> { (key, value) -> java.util.Map.Entry.key },
                    Collectors.mapping<Map.Entry<String?, String>, String, Any, List<String>>(
                        Function<Map.Entry<String?, String>, String> { (key, value) -> java.util.Map.Entry.value }, Collectors.toList()
                    )
                )
            )
    }

    fun trialsForDriver(driver: Driver): List<String> {
        return Optional.ofNullable(eligibleTrialsByInclusionEvent[driver.event()]).orElse(emptyList())
    }

    fun driverIsActionable(driver: Driver): Boolean {
        return (!driver.evidence().externalEligibleTrials().isEmpty() || eligibleTrialsByInclusionEvent.containsKey(driver.event())
                || !driver.evidence().approvedTreatments().isEmpty())
    }
}