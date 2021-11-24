package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class MolecularResultsAreAvailable implements EvaluationFunction {

    MolecularResultsAreAvailable() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        // This was implemented when ACTIN requires a mandatory molecular record.
        return Evaluation.PASS;
    }
}
