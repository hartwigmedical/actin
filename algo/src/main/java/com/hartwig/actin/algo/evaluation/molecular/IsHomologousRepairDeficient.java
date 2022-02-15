package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsHomologousRepairDeficient implements EvaluationFunction {

    IsHomologousRepairDeficient() {
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Boolean isHomologousRepairDeficient = record.molecular().isHomologousRepairDeficient();

        if (isHomologousRepairDeficient == null) {
            return EvaluationResult.UNDETERMINED;
        } else if (isHomologousRepairDeficient) {
            return EvaluationResult.PASS;
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
