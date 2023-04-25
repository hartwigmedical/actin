package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class MeetsSpecificBiopsyRequirements implements EvaluationFunction {

    MeetsSpecificBiopsyRequirements() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                        "This evaluation requires specific requirements regarding biopsy that currently cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined specific biopsy requirements")
                .build();
    }
}
