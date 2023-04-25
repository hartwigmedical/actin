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
        Boolean recoverable = null;
        for (Evaluation eval : evaluations) {
            if (worst == null || eval.result().isWorseThan(worst)) {
                worst = eval.result();
                recoverable = eval.recoverable();
            } else if (worst == eval.result()) {
                recoverable = eval.recoverable() && recoverable;
            }
        }

        if (worst == null || recoverable == null) {
            throw new IllegalStateException("Could not determine AND result for functions: " + functions);
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(worst).recoverable(recoverable);
        for (Evaluation eval : evaluations) {
            if (eval.result() == worst && eval.recoverable() == recoverable) {
                builder.addAllInclusionMolecularEvents(eval.inclusionMolecularEvents());
                builder.addAllExclusionMolecularEvents(eval.exclusionMolecularEvents());
                builder.addAllPassSpecificMessages(eval.passSpecificMessages());
                builder.addAllPassGeneralMessages(eval.passGeneralMessages());
                builder.addAllWarnSpecificMessages(eval.warnSpecificMessages());
                builder.addAllWarnGeneralMessages(eval.warnGeneralMessages());
                builder.addAllUndeterminedSpecificMessages(eval.undeterminedSpecificMessages());
                builder.addAllUndeterminedGeneralMessages(eval.undeterminedGeneralMessages());
                builder.addAllFailSpecificMessages(eval.failSpecificMessages());
                builder.addAllFailGeneralMessages(eval.failGeneralMessages());
            }
        }

        return builder.build();
    }
}
