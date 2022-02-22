package com.hartwig.actin.algo.evaluation.composite;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class Or implements EvaluationFunction {

    @NotNull
    private final List<EvaluationFunction> functions;

    public Or(@NotNull final List<EvaluationFunction> functions) {
        this.functions = functions;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<Evaluation> evaluations = Sets.newHashSet();
        for (EvaluationFunction function : functions) {
            evaluations.add(function.evaluate(record));
        }

        EvaluationResult best = null;
        for (Evaluation eval : evaluations) {
            if (best == null || best.isWorseThan(eval.result())) {
                best = eval.result();
            }
        }

        if (best == null) {
            throw new IllegalStateException("Could not determine OR result for functions: " + functions);
        }

        Set<String> passMessages = Sets.newHashSet();
        Set<String> undeterminedMessages = Sets.newHashSet();
        Set<String> failMessages = Sets.newHashSet();

        for (Evaluation eval : evaluations) {
            if (eval.result() == best) {
                passMessages.addAll(eval.passMessages());
                undeterminedMessages.addAll(eval.undeterminedMessages());
                failMessages.addAll(eval.failMessages());
            }
        }

        return ImmutableEvaluation.builder()
                .result(best)
                .passMessages(passMessages)
                .undeterminedMessages(undeterminedMessages)
                .failMessages(failMessages)
                .build();
    }
}
