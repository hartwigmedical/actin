package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;

public class InferredTumorStageEvaluationFunction implements EvaluationFunction {

    private final InferredTumorStage inferredTumorStage;
    private final EvaluationFunction originalFunction;

    public InferredTumorStageEvaluationFunction(final InferredTumorStage inferredTumorStage, final EvaluationFunction originalFunction) {
        this.inferredTumorStage = inferredTumorStage;
        this.originalFunction = originalFunction;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        return inferredTumorStage.from(record.clinical().tumor())
                .stream()
                .map(s -> originalFunction.evaluate(ImmutablePatientRecord.copyOf(record)
                        .withClinical(ImmutableClinicalRecord.copyOf(record.clinical())
                                .withTumor(ImmutableTumorDetails.copyOf(record.clinical().tumor()).withStage(s)))))
                .min((e1, e2) -> e1.result().equals(e2.result()) ? 0 : e1.result().isWorseThan(e2.result()) ? 1 : -1)
                .orElse(originalFunction.evaluate(record));
    }
}
