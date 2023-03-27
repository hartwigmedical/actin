package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class HasLocallyAdvancedCancer implements EvaluationFunction {

    public static final String DISPLAY_NAME = "locally advanced";

    HasLocallyAdvancedCancer() {
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
            result = EvaluationResult.PASS;
        } else if (isStageMatch(stage, TumorStage.II)) {
            result = EvaluationResult.WARN;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result).displayName(DISPLAY_NAME);
        if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor stage " + stage + " is considered locally advanced");
            builder.addPassGeneralMessages("Locally advanced cancer");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Could not be determined if tumor stage " + stage + " is considered locally advanced");
            builder.addWarnGeneralMessages("Locally advanced cancer");
        } else if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor stage " + stage + " is not considered locally advanced");
            builder.addFailGeneralMessages("No locally advanced cancer");
        }

        return builder.build();
    }

    private static boolean isStageMatch(@NotNull TumorStage stage, @NotNull TumorStage stageToMatch) {
        return stage == stageToMatch || stage.category() == stageToMatch;
    }
}
