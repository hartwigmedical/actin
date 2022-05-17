package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadTreatmentWithCategoryOfTypesWithinWeeks implements EvaluationFunction {

    //TODO: Implement according to README
    HasHadTreatmentWithCategoryOfTypesWithinWeeks() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Treatment of certain type within certain weeks currently cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined treatment within nr of weeks")
                .build();
    }

}
