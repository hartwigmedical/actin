package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class PositiveForCD8TCellsByIHC implements EvaluationFunction {

    PositiveForCD8TCellsByIHC() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("It is currently undetermined if the sample is positive for CD8 T-Cells")
                .addUndeterminedGeneralMessages("Undetermined CD8 T-cell positivity status")
                .build();
    }
}