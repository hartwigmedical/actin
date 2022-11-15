package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: implement according to README
public class HasSufficientMeasuredCreatinineClearance implements EvaluationFunction {

    HasSufficientMeasuredCreatinineClearance() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently directly measured creatinine clearance cannot be detetermined")
                .addUndeterminedGeneralMessages("Undetermined measured creatinine clearance")
                .build();
    }
}
