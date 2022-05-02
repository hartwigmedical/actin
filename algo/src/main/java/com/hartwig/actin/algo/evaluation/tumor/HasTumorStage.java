package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Tumor stage details are missing")
                    .addUndeterminedGeneralMessages("Missing tumor stage details")
                    .build();
        }

        boolean hasTumorStage = stage == stageToMatch || stage.category() == stageToMatch;

        EvaluationResult result = hasTumorStage ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient tumor stage is not exact stage " + stageToMatch.display());
            builder.addFailGeneralMessages("Inadequate tumor stage");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient tumor stage is exact stage " + stageToMatch.display());
            builder.addPassGeneralMessages("Adequate tumor stage");
        }

        return builder.build();
    }
}
