package com.hartwig.actin.algo.soc;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment;

import org.jetbrains.annotations.NotNull;

class EvaluatedTreatmentInterpreter {

    @NotNull
    private final List<EvaluatedTreatment> recommendedTreatments;

    EvaluatedTreatmentInterpreter(@NotNull List<EvaluatedTreatment> recommendedTreatments) {
        this.recommendedTreatments = recommendedTreatments;
    }

    @NotNull
    String summarize() {
        if (recommendedTreatments.isEmpty()) {
            return "No treatments available";
        } else {
            int bestScore = recommendedTreatments.get(0).score();
            return "Recommended treatments: " + recommendedTreatments.stream()
                    .filter(t -> t.score() == bestScore)
                    .map(evaluatedTreatment -> evaluatedTreatment.treatment().name())
                    .collect(Collectors.joining(", "));
        }
    }

    @NotNull
    String csv() {
        return "Treatment,Score,Warnings\n" + recommendedTreatments.stream().map(t -> {
            String warningSummary = t.evaluations()
                    .stream()
                    .flatMap(eval -> Stream.of(eval.failSpecificMessages(),
                            eval.warnSpecificMessages(),
                            eval.undeterminedSpecificMessages()).flatMap(Collection::stream))
                    .collect(Collectors.joining(";"));
            return String.join(",", t.treatment().name(), Integer.toString(t.score()), warningSummary);
        }).collect(Collectors.joining("\n"));
    }

    @NotNull
    String listAvailableTreatmentsByScore() {
        return availableTreatmentsByScore().entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .map(entry -> "Score=" + entry.getKey() + ": " + entry.getValue()
                        .stream()
                        .map(evaluatedTreatment -> evaluatedTreatment.treatment().name())
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n"));
    }

    private Map<Integer, List<EvaluatedTreatment>> availableTreatmentsByScore() {
        return recommendedTreatments.stream().collect(groupingBy(EvaluatedTreatment::score));
    }
}
