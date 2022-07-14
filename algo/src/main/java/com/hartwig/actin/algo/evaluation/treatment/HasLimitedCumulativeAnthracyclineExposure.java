package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Implement according to README
public class HasLimitedCumulativeAnthracyclineExposure implements EvaluationFunction {

    HasLimitedCumulativeAnthracyclineExposure() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently it is undetermined if patient meets anthracycline criteria")
                .addUndeterminedGeneralMessages("Undetermined anthracycline exposure")
                .build();
    }
}
