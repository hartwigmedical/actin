package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import org.jetbrains.annotations.NotNull;

// TODO: Update according to README
public class HasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCycles implements EvaluationFunction {

    HasProgressiveDiseaseFollowingTypedTreatmentsOfCategoryAndMinimumCycles() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("PD following certain treatment with minimal nr of cycles currently cannot be determined")
                .addUndeterminedGeneralMessages("Treatment history with PD & minimal nr of cycles")
                .build();
    }
}
