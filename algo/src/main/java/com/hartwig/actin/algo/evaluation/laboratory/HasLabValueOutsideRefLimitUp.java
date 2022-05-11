package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Implement according to README
public class HasLabValueOutsideRefLimitUp implements EvaluationFunction {

    HasLabValueOutsideRefLimitUp() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Lab value outside upper ref limit currently cannot be determined")
                .build();
    }

}
