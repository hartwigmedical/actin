package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class HasTumorStage implements EvaluationFunction {

    @NotNull
    private final TumorStage stageToMatch;

    HasTumorStage(@NotNull final TumorStage stageToMatch) {
        this.stageToMatch = stageToMatch;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        TumorStage stage = record.clinical().tumor().stage();
        if (stage == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Tumor stage is missing")
                    .build();
        }

        boolean hasTumorStage = stage == stageToMatch || stage.category() == stageToMatch;
        EvaluationResult result = hasTumorStage ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient does not meet required tumor stage " + stageToMatch);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient meets required tumor stage " + stageToMatch);
        }

        return builder.build();
    }
}
