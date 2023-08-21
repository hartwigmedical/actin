package com.hartwig.actin.report.interpretation;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.hartwig.actin.molecular.datamodel.driver.Driver;

public class EvaluatedCohortsInterpreter {

    private final Map<String, List<String>> eligibleTrialsByInclusionEvent;

    public EvaluatedCohortsInterpreter(List<EvaluatedCohort> evaluatedCohorts) {
        eligibleTrialsByInclusionEvent = evaluatedCohorts.stream()
                .filter(EvaluatedCohort::isPotentiallyEligible)
                .filter(EvaluatedCohort::isOpen)
                .flatMap(cohort -> cohort.molecularEvents().stream().map(event -> Maps.immutableEntry(event, cohort.acronym())))
                .sorted(Map.Entry.comparingByValue())
                .collect(groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    public List<String> trialsForDriver(Driver driver) {
        return Optional.ofNullable(eligibleTrialsByInclusionEvent.get(driver.event())).orElse(Collections.emptyList());
    }

    public boolean driverIsActionable(Driver driver) {
        return !driver.evidence().externalEligibleTrials().isEmpty() || eligibleTrialsByInclusionEvent.containsKey(driver.event())
                || !driver.evidence().approvedTreatments().isEmpty();
    }
}
