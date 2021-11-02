package com.hartwig.actin.algo.evaluation.composite;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class And implements EvaluationFunction {

    @NotNull
    private final EvaluationFunction function1;
    @NotNull
    private final EvaluationFunction function2;

    public And(@NotNull final EvaluationFunction function1, @NotNull final EvaluationFunction function2) {
        this.function1 = function1;
        this.function2 = function2;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<Evaluation> results = Lists.newArrayList(function1.evaluate(record), function2.evaluate(record));

        if (results.contains(Evaluation.COULD_NOT_BE_DETERMINED)) {
            return Evaluation.COULD_NOT_BE_DETERMINED;
        } else if (results.contains(Evaluation.FAIL)) {
            return Evaluation.FAIL;
        } else if (results.contains(Evaluation.PASS_BUT_WARN)) {
            return Evaluation.PASS_BUT_WARN;
        } else {
            return Evaluation.PASS;
        }
    }
}
