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

public class And implements EvaluationFunction {

    @NotNull
    private final List<EvaluationFunction> functions;

    public And(@NotNull final List<EvaluationFunction> functions) {
        this.functions = functions;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<Evaluation> evaluations = Sets.newHashSet();
        for (EvaluationFunction function : functions) {
            evaluations.add(function.evaluate(record));
        }

        EvaluationResult worst = null;
        for (Evaluation eval : evaluations) {
            if (worst == null || eval.result().isWorseThan(worst)) {
                worst = eval.result();
            }
        }

        if (worst == null) {
            throw new IllegalStateException("Could not determine AND result for functions: " + functions);
        }

        Set<String> passMessages = Sets.newHashSet();
        Set<String> undeterminedMessages = Sets.newHashSet();
        Set<String> failMessages = Sets.newHashSet();

        for (Evaluation eval : evaluations) {
            if (eval.result() == worst) {
                passMessages.addAll(eval.passMessages());
                undeterminedMessages.addAll(eval.undeterminedMessages());
                failMessages.addAll(eval.failMessages());
            }
        }

        return ImmutableEvaluation.builder()
                .result(worst)
                .passMessages(passMessages)
                .undeterminedMessages(undeterminedMessages)
                .failMessages(failMessages)
                .build();
    }
}
