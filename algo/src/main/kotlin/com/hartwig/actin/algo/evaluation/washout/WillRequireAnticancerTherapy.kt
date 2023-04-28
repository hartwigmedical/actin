package com.hartwig.actin.algo.evaluation.washout;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class WillRequireAnticancerTherapy implements EvaluationFunction {

    WillRequireAnticancerTherapy() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.NOT_EVALUATED)
                .addPassSpecificMessages("Currently not evaluated whether anticancer therapy is required")
                .addPassGeneralMessages("Anticancer therapy requirement not assumed")
                .build();
    }
}
