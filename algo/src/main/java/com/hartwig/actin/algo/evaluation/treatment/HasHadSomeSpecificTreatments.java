package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeSpecificTreatments implements EvaluationFunction {

    @NotNull
    private final Set<String> names;
    private final int minTreatmentLines;

    HasHadSomeSpecificTreatments(@NotNull final Set<String> names, final int minTreatmentLines) {
        this.names = names;
        this.minTreatmentLines = minTreatmentLines;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> treatments = Sets.newHashSet();
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            for (String name : names) {
                if (treatment.name().toLowerCase().contains(name.toLowerCase())) {
                    treatments.add(treatment.name());
                }
            }
        }

        EvaluationResult result = treatments.size() >= minTreatmentLines ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has not received " + Format.concat(names) + " at least " + minTreatmentLines + " times");
        } else if (result.isPass()) {
            builder.addPassMessages("Patient has received " + Format.concat(treatments) + " " + treatments.size() + " times");
        }

        return builder.build();
    }
}
