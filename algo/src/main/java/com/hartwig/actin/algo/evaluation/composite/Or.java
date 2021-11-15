package com.hartwig.actin.algo.evaluation.composite;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
        List<Evaluation> evaluations = Lists.newArrayList();
        for (EvaluationFunction function : functions) {
            evaluations.add(function.evaluate(record));
        }

        if (evaluations.contains(Evaluation.PASS)) {
            return Evaluation.PASS;
        } else if (evaluations.contains(Evaluation.PASS_BUT_WARN)) {
            return Evaluation.PASS_BUT_WARN;
        } else if (evaluations.contains(Evaluation.NOT_IMPLEMENTED)) {
            return Evaluation.NOT_IMPLEMENTED;
        } else if (evaluations.contains(Evaluation.UNDETERMINED)) {
            return Evaluation.UNDETERMINED;
        } else if (evaluations.contains(Evaluation.FAIL)) {
            return Evaluation.FAIL;
        }

        throw new IllegalStateException("OR could not combine evaluations: " + evaluations);
    }
}
