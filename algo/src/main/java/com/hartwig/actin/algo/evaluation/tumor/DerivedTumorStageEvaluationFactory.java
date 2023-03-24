package com.hartwig.actin.algo.evaluation.tumor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class DerivedTumorStageEvaluationFactory {

    public static Evaluation follow(Map.Entry<TumorStage, Evaluation> derived) {
        Evaluation toFollow = derived.getValue();
        return ImmutableEvaluation.builder().from(toFollow).build();
    }

    static Evaluation pass(Map<TumorStage, Evaluation> derived) {
        Evaluation firstPass = derived.values().iterator().next();
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllPassSpecificMessages(passedMessage(derived, firstPass.passSpecificMessages()))
                .addAllPassGeneralMessages(passedMessage(derived, firstPass.passGeneralMessages()))
                .build();
    }

    static Evaluation undetermined(Map<TumorStage, Evaluation> derived) {
        Map.Entry<TumorStage, Evaluation> passingDerivation =
                derived.entrySet().stream().filter(e -> e.getValue().result().equals(EvaluationResult.PASS)).findFirst().orElseThrow();
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(undeterminedMessage(derived,
                        passingDerivation,
                        passingDerivation.getValue().undeterminedSpecificMessages()))
                .addUndeterminedGeneralMessages(undeterminedMessage(derived,
                        passingDerivation,
                        passingDerivation.getValue().undeterminedGeneralMessages()))
                .build();
    }

    static Evaluation warn(Map<TumorStage, Evaluation> derived) {
        List<Map.Entry<TumorStage, Evaluation>> warningDerivations =
                derived.entrySet().stream().filter(e -> e.getValue().result().equals(EvaluationResult.WARN)).collect(Collectors.toList());
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.WARN)
                .addWarnSpecificMessages(warnMessage(derived,
                        warningDerivations,
                        warningDerivations.stream().flatMap(e -> e.getValue().warnSpecificMessages().stream()).collect(Collectors.toSet())))
                .addWarnGeneralMessages(warnMessage(derived,
                        warningDerivations,
                        warningDerivations.stream().flatMap(e -> e.getValue().warnGeneralMessages().stream()).collect(Collectors.toSet())))
                .build();
    }

    static Evaluation fail(Map<TumorStage, Evaluation> derived) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(failMessage(derived))
                .addFailGeneralMessages(failMessage(derived))
                .build();
    }

    private static List<String> passedMessage(Map<TumorStage, Evaluation> derived, Set<String> messages) {
        return List.of(String.format("%s, and evaluation passes for all with message [%s]", preamble(derived), joinedMessages(messages)));
    }

    private static String warnMessage(Map<TumorStage, Evaluation> derived, List<Map.Entry<TumorStage, Evaluation>> warningDerivations,
            Set<String> messages) {
        return String.format("%s Tumor stages [%s] had warnings with messages [%s], hence the result the result is a warning",
                preamble(derived),
                stagesFrom(warningDerivations.stream().map(Map.Entry::getKey)),
                joinedMessages(messages));
    }

    private static String failMessage(Map<TumorStage, Evaluation> derived) {
        return String.format("%s None of these possible staged passed on warned, hence the result is a failure", preamble(derived));
    }

    private static String undeterminedMessage(Map<TumorStage, Evaluation> derived, Map.Entry<TumorStage, Evaluation> passingDerivation,
            Set<String> messages) {
        return String.format(
                "%s Only tumor stage [%s] passed with message [%s], but the others did not, hence the result the is undetermined",
                preamble(derived),
                passingDerivation.getKey(),
                joinedMessages(messages));
    }

    @NotNull
    private static String preamble(Map<TumorStage, Evaluation> derived) {
        return String.format("Tumor stage details are missing. Based on lesion localization tumor stage should be [%s].",
                stagesFrom(derived.keySet().stream()));
    }

    @NotNull
    private static String joinedMessages(final Set<String> firstPass) {
        return String.join(",", firstPass);
    }

    private static String stagesFrom(Stream<TumorStage> stream) {
        return stream.sorted().map(TumorStage::toString).collect(Collectors.joining(" or "));
    }
}
