package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadSomeApprovedTreatments implements EvaluationFunction {

    private final int minApprovedTreatments;

    HasHadSomeApprovedTreatments(final int minApprovedTreatments) {
        this.minApprovedTreatments = minApprovedTreatments;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.clinical().priorTumorTreatments().isEmpty() && minApprovedTreatments > 0) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has had no prior tumor treatment")
                    .addFailGeneralMessages("No prior treatment")
                    .build();
        }
        
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently the number of approved treatments cannot be determined")
                .addUndeterminedGeneralMessages("Approved treatments")
                .build();
    }
}
