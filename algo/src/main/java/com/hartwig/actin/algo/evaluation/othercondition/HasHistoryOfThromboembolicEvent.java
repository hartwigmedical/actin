package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import org.jetbrains.annotations.NotNull;

//TODO: update according to README
public class HasHistoryOfThromboembolicEvent implements EvaluationFunction {

    HasHistoryOfThromboembolicEvent() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently history regarding thromboembolic events cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined history of thromboembolic events")
                .build();
    }
}
