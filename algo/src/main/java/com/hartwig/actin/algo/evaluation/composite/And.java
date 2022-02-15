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
        EvaluationResult worst = null;
        Set<String> messages = Sets.newHashSet();
        for (EvaluationFunction function : functions) {
            Evaluation eval = function.evaluate(record);
            if (worst == null || eval.result().isWorseThan(worst)) {
                worst = eval.result();
            }
            messages.addAll(eval.messages());
        }

        if (worst == null) {
            throw new IllegalStateException("Could not determine AND result for functions: " + functions);
        }

        return ImmutableEvaluation.builder().result(worst).messages(messages).build();
    }
}
