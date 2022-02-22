package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatment implements EvaluationFunction {

    @NotNull
    private final String name;

    HasHadTreatment(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.name().contains(name)) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassMessages("Patient has received treatment " + name)
                        .build();
            }
        }

        return EvaluationFactory.create(EvaluationResult.FAIL); // TODO add working message
    }
}
