package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class Not implements EvaluationFunction {

    @NotNull
    private final EvaluationFunction function;

    public Not(@NotNull final EvaluationFunction function) {
        this.function = function;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        EvaluationResult result = function.evaluate(record).result();

        if (result == EvaluationResult.PASS || result == EvaluationResult.PASS_BUT_WARN) {
            return EvaluationFactory.create(EvaluationResult.FAIL);
        } else if (result == EvaluationResult.FAIL) {
            return EvaluationFactory.create(EvaluationResult.PASS);
        } else if (result == EvaluationResult.UNDETERMINED || result == EvaluationResult.NOT_IMPLEMENTED
                || result == EvaluationResult.NOT_EVALUATED) {
            return EvaluationFactory.create(result);
        }

        throw new IllegalStateException("NOT function cannot negate evaluation: " + result);
    }
}
