package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeTreatmentsOfCategoryWithStopReasonPD implements EvaluationFunction {

    //TODO: Implement according to README
    HasHadSomeTreatmentsOfCategoryWithStopReasonPD() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Stop reason for a certain category currently cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined treatment category with stop reason")
                .build();
    }
}
