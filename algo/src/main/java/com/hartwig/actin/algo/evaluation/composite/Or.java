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
        EvaluationResult best = null;
        Set<String> messages = Sets.newHashSet();
        for (EvaluationFunction function : functions) {
            Evaluation eval = function.evaluate(record);
            if (best == null || best.isWorseThan(eval.result())) {
                best = eval.result();
            }
            messages.addAll(eval.messages());
        }

        if (best == null) {
            throw new IllegalStateException("Could not determine OR result for functions: " + functions);
        }

        return ImmutableEvaluation.builder().result(best).messages(messages).build();
    }
}
