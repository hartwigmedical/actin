package com.hartwig.actin.algo.evaluation.composite;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        List<EvaluationResult> evaluations = Lists.newArrayList();
        for (EvaluationFunction function : functions) {
            evaluations.add(function.evaluate(record));
        }

        if (evaluations.contains(EvaluationResult.PASS)) {
            return EvaluationResult.PASS;
        } else if (evaluations.contains(EvaluationResult.PASS_BUT_WARN)) {
            return EvaluationResult.PASS_BUT_WARN;
        } else if (evaluations.contains(EvaluationResult.NOT_EVALUATED)) {
            return EvaluationResult.NOT_EVALUATED;
        } else if (evaluations.contains(EvaluationResult.NOT_IMPLEMENTED)) {
            return EvaluationResult.NOT_IMPLEMENTED;
        } else if (evaluations.contains(EvaluationResult.UNDETERMINED)) {
            return EvaluationResult.UNDETERMINED;
        } else if (evaluations.contains(EvaluationResult.FAIL)) {
            return EvaluationResult.FAIL;
        }

        throw new IllegalStateException("OR could not combine evaluations: " + evaluations);
    }
}
