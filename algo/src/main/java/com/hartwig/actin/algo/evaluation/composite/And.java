package com.hartwig.actin.algo.evaluation.composite;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
        List<Evaluation> results = Lists.newArrayList();
        for (EvaluationFunction function : functions) {
            results.add(function.evaluate(record));
        }

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
