package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasRecurrentCancer implements EvaluationFunction {

    private static final String DISPLAY_NAME = "has recurrent cancer";

    HasRecurrentCancer() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .displayName(DISPLAY_NAME)
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently cannot be determined if cancer is recurrent")
                .addUndeterminedGeneralMessages("Undetermined recurrent cancer")
                .build();
    }
}