package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
public class RequiresRegularHematopoieticSupport implements EvaluationFunction {

    RequiresRegularHematopoieticSupport() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently regular hematopoietic support cannot be determined")
                .addUndeterminedSpecificMessages("Undetermined hematopoietic support")
                .build();
    }
}
