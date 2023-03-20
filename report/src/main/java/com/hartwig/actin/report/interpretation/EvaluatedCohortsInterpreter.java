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
    private final Map<String, List<String>> trialsByInclusionEvent;

    public EvaluatedCohortsInterpreter(List<EvaluatedCohort> evaluatedCohorts) {
        trialsByInclusionEvent = evaluatedCohorts.stream()
                .filter(EvaluatedCohort::isPotentiallyEligible)
                .filter(EvaluatedCohort::isOpen)
                .flatMap(cohort -> cohort.molecularEvents().stream().map(event -> Maps.immutableEntry(event, cohort.acronym())))
                .sorted(Map.Entry.comparingByValue())
                .collect(groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    public boolean hasTrialMatchingEvent(String event) {
        return trialsByInclusionEvent.containsKey(event);
    }

    public List<String> getTrialsForDriver(Driver driver) {
        return Optional.ofNullable(trialsByInclusionEvent.get(driver.event())).orElse(Collections.emptyList());
    }
}
