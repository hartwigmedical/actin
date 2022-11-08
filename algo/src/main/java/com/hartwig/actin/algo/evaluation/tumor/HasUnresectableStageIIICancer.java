package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class HasUnresectableStageIIICancer implements EvaluationFunction {

    HasUnresectableStageIIICancer() {
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

        EvaluationResult result;
        if (isStageMatch(stage, TumorStage.III)) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no unresectable stage III cancer");
            builder.addFailGeneralMessages("No unresectable stage III cancer");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("Undetermined if stage III cancer is considered unresectable");
            builder.addUndeterminedGeneralMessages("Undetermined stage III resectability");
        }

        return builder.build();
    }

    private static boolean isStageMatch(@NotNull TumorStage stage, @NotNull TumorStage stageToMatch) {
        return stage == stageToMatch || stage.category() == stageToMatch;
    }
}
