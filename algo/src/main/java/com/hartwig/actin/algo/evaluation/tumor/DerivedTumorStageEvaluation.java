package com.hartwig.actin.algo.evaluation.tumor;

import static java.lang.String.format;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.TumorStage;

final class DerivedTumorStageEvaluation {

    static Evaluation create(Map<TumorStage, Evaluation> derived, BiFunction<String, String, Evaluation> createEvaluation) {
        Evaluation worstEvaluation = worstEvaluation(derived);
        return createEvaluation.apply(allSpecificMessagesFrom(derived, worstEvaluation), allGeneralMessagesFrom(worstEvaluation));
    }

    private static Evaluation worstEvaluation(Map<TumorStage, Evaluation> derived) {
        return derived.values().stream().min((e1, e2) -> e1.equals(e2) ? 0 : e1.result().isWorseThan(e2.result()) ? -1 : 1).orElseThrow();
    }

    private static String allSpecificMessagesFrom(Map<TumorStage, Evaluation> derived, Evaluation worstEvaluation) {
        return format("%s. Tumor stage has been implied to be %s",
                Stream.of(worstEvaluation.passSpecificMessages(),
                        worstEvaluation.warnSpecificMessages(),
                        worstEvaluation.failSpecificMessages(),
                        worstEvaluation.undeterminedSpecificMessages()).flatMap(Set::stream).collect(Collectors.joining(". ")),
                stagesFrom(derived.keySet().stream()));
    }

    private static String allGeneralMessagesFrom(Evaluation worstEvaluation) {
        return Stream.of(worstEvaluation.passGeneralMessages(),
                worstEvaluation.warnGeneralMessages(),
                worstEvaluation.failGeneralMessages(),
                worstEvaluation.undeterminedGeneralMessages()).flatMap(Set::stream).collect(Collectors.joining(". "));
    }

    private static String stagesFrom(Stream<TumorStage> stream) {
        return stream.sorted().map(TumorStage::toString).collect(Collectors.joining(" or "));
    }
}
