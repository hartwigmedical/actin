package com.hartwig.actin.algo.evaluation.complication;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasSpecificComplication implements EvaluationFunction {

    @NotNull
    private final String termToFind;

    HasSpecificComplication(@NotNull final String termToFind) {
        this.termToFind = termToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> complications = Sets.newHashSet();
        for (Complication complication : record.clinical().complications()) {
            if (complication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                complications.add(complication.name());
            }
        }

        if (!complications.isEmpty()) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.PASS)
                    .addPassMessages("Patient has complication " + concat(complications))
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Patient does not have complication " + termToFind)
                .build();
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
