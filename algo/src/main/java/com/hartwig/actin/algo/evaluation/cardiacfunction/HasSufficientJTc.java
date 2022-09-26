package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
public class HasSufficientJTc implements EvaluationFunction {

    HasSufficientJTc() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("JTc currently cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined JTc")
                .build();
    }
}
