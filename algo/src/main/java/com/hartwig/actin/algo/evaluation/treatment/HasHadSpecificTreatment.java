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

public class HasHadSpecificTreatment implements EvaluationFunction {

    @NotNull
    private final Set<String> names;

    HasHadSpecificTreatment(@NotNull final Set<String> names) {
        this.names = names;
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

        EvaluationResult result = !treatments.isEmpty() ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has not received " + Format.concat(names));
        } else if (result.isPass()) {
            builder.addPassMessages("Patient has received " + Format.concat(treatments));
        }

        return builder.build();
    }
}
