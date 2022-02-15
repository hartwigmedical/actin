package com.hartwig.actin.algo.evaluation.composite;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
        List<Evaluation> evaluations = Lists.newArrayList();
        for (EvaluationFunction function : functions) {
            evaluations.add(function.evaluate(record));
        }

        Set<EvaluationResult> results = extractResults(evaluations);
        if (results.contains(EvaluationResult.FAIL)) {
            return EvaluationFactory.create(EvaluationResult.FAIL);
        } else if (results.contains(EvaluationResult.UNDETERMINED)) {
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        } else if (results.contains(EvaluationResult.NOT_IMPLEMENTED)) {
            return EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED);
        } else if (results.contains(EvaluationResult.PASS_BUT_WARN)) {
            return EvaluationFactory.create(EvaluationResult.PASS_BUT_WARN);
        } else if (results.contains(EvaluationResult.PASS)) {
            return EvaluationFactory.create(EvaluationResult.PASS);
        } else if (results.contains(EvaluationResult.NOT_EVALUATED)) {
            return EvaluationFactory.create(EvaluationResult.NOT_EVALUATED);
        }

        throw new IllegalStateException("AND could not combine evaluation results: " + results);
    }

    @NotNull
    private static Set<EvaluationResult> extractResults(@NotNull List<Evaluation> evaluations) {
        Set<EvaluationResult> results = Sets.newHashSet();
        for (Evaluation evaluation : evaluations) {
            results.add(evaluation.result());
        }
        return results;
    }
}
