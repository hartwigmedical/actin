package com.hartwig.actin.algo.evaluation.complication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasUncontrolledTumorRelatedPain implements EvaluationFunction {

    //TODO: Implement according to README
    HasUncontrolledTumorRelatedPain() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Uncontrolled tumor related pain currently cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined uncontrolled tumor related pain")
                .build();
    }
}
