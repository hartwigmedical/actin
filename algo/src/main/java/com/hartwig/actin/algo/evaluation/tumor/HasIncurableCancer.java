package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class HasIncurableCancer implements EvaluationFunction {

    HasIncurableCancer() {
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
        if (isStageMatch(stage, TumorStage.IV)) {
            result = EvaluationResult.PASS;
        } else if (isStageMatch(stage, TumorStage.III)) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient should have no incurable cancer");
            builder.addFailGeneralMessages("No incurable cancer");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("Undetermined if patient has incurable cancer");
            builder.addUndeterminedGeneralMessages("Undetermined if cancer incurable");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Stage IV cancer is considered incurable");
            builder.addPassGeneralMessages("Incurable cancer");
        }

        return builder.build();
    }

    private static boolean isStageMatch(@NotNull TumorStage stage, @NotNull TumorStage stageToMatch) {
        return stage == stageToMatch || stage.category() == stageToMatch;
    }
}
