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

public class HasHadLimitedSpecificTreatments implements EvaluationFunction {

    @NotNull
    private final Set<String> names;
    private final int maxTreatmentLines;

    HasHadLimitedSpecificTreatments(@NotNull final Set<String> names, final int maxTreatmentLines) {
        this.names = names;
        this.maxTreatmentLines = maxTreatmentLines;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        Set<String> treatments = Sets.newHashSet();
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            for (String name : names) {
                if (treatment.name().toLowerCase().contains(name.toLowerCase())) {
                    treatments.add(treatment.name());
                }
            }
        }

        EvaluationResult result = treatments.size() <= maxTreatmentLines ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has received " + Format.concat(names) + " more than " + maxTreatmentLines + " times");
        } else if (result.isPass()) {
            builder.addPassMessages("Patient has received " + Format.concat(treatments) + " " + treatments.size() + " times");
        }

        return builder.build();
    }
}
