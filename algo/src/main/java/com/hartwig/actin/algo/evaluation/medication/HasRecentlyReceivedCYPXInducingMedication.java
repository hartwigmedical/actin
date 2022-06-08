package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedCYPXInducingMedication implements EvaluationFunction {

    @NotNull
    private final String termToFind;

    HasRecentlyReceivedCYPXInducingMedication(@NotNull final String termToFind) {
        this.termToFind = termToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                        "Currently not determined if patient has recently received " + termToFind + " inducing medication")
                .addUndeterminedGeneralMessages("CYP medication requirements")
                .build();
    }
}
