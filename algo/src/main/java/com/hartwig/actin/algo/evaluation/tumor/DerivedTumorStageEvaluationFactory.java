package com.hartwig.actin.algo.evaluation.tumor;

import static java.lang.String.format;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.TumorStage;

public class DerivedTumorStageEvaluationFactory {

    public static Evaluation follow(Map.Entry<TumorStage, Evaluation> derived) {
        switch (derived.getValue().result()) {
            case PASS:
                return pass(mapOf(derived));
            case UNDETERMINED:
                return undetermined(mapOf(derived));
            case WARN:
                return warn(mapOf(derived));
            default:
                return fail(mapOf(derived));
        }
    }

    private static Map<TumorStage, Evaluation> mapOf(Map.Entry<TumorStage, Evaluation> derived) {
        return Map.of(derived.getKey(), derived.getValue());
    }

    static Evaluation pass(Map<TumorStage, Evaluation> derived) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages(format("%s %s.",
                        preamble(derived),
                        stageImpliedMessages(derived, Evaluation::passSpecificMessages)))
                .addPassGeneralMessages(format("Derived tumor stage of %s %s", stagesFrom(derived.keySet().stream()), displayName(derived)))
                .build();
    }

    static Evaluation undetermined(Map<TumorStage, Evaluation> derived) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(format("%s %s It is undetermined whether the patient %s.",
                        preamble(derived),
                        stageImpliedMessages(derived, Evaluation::undeterminedSpecificMessages),
                        displayName(derived)))
                .addUndeterminedGeneralMessages(format("From derived tumor stage of %s it is undetermined if %s",
                        stagesFrom(derived.keySet().stream()),
                        displayName(derived)))
                .build();
    }

    static Evaluation warn(Map<TumorStage, Evaluation> derived) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.WARN)
                .addWarnSpecificMessages(format("%s %s.",
                        preamble(derived),
                        stageImpliedMessages(derived, Evaluation::warnSpecificMessages)))
                .addWarnGeneralMessages(format("Derived tumor stage of %s %s", stagesFrom(derived.keySet().stream()), displayName(derived)))
                .build();
    }

    static Evaluation fail(Map<TumorStage, Evaluation> derived) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(format("%s %s.",
                        preamble(derived),
                        stageImpliedMessages(derived, Evaluation::failSpecificMessages)))
                .addFailGeneralMessages(format("Derived tumor stage of %s %s",
                        stagesFrom(derived.keySet().stream()),
                        negate(displayName(derived))))
                .build();
    }

    private static String displayName(Map<TumorStage, Evaluation> derived) {
        return derived.values()
                .stream()
                .findFirst()
                .flatMap(e -> Optional.ofNullable(e.displayName()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "All evaluation functions used with derived tumor stage must define a display name"));
    }

    private static String negate(String displayName) {
        return displayName.replace("has", "does not have").replace("is", "is not");
    }

    private static String stageImpliedMessages(Map<TumorStage, Evaluation> derived, Function<Evaluation, Set<String>> extractingMessages) {
        return derived.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .map(evaluation -> format(String.join("; ", extractingMessages.apply(evaluation))))
                .collect(Collectors.joining(". "));
    }

    private static String preamble(Map<TumorStage, Evaluation> derived) {
        return format("Tumor stage details are missing but based on lesion localization tumor stage should be %s.",
                stagesFrom(derived.keySet().stream()));
    }

    private static String stagesFrom(Stream<TumorStage> stream) {
        return stream.sorted().map(TumorStage::toString).collect(Collectors.joining(" or "));
    }
}
