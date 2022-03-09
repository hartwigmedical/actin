package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator;

import org.jetbrains.annotations.NotNull;

public class HasSufficientBodyWeight implements EvaluationFunction {

    static final String EXPECTED_UNIT = "kilogram";

    private final double minBodyWeight;

    HasSufficientBodyWeight(final double minBodyWeight) {
        this.minBodyWeight = minBodyWeight;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<BodyWeight> weights = Lists.newArrayList(record.clinical().bodyWeights());

        if (weights.isEmpty()) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("No body weights found")
                    .build();
        }

        weights.sort(new BodyWeightDescendingDateComparator());

        BodyWeight mostRecent = weights.get(0);

        if (!mostRecent.unit().equalsIgnoreCase(EXPECTED_UNIT)) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Most recent body weight not measured in " + EXPECTED_UNIT)
                    .build();
        }

        EvaluationResult result = Double.compare(mostRecent.value(), minBodyWeight) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has body weight below " + minBodyWeight);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient has body weight above " + minBodyWeight);
        }

        return builder.build();
    }
}