package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;

import org.jetbrains.annotations.NotNull;

public class IsHomologousRepairDeficient implements EvaluationFunction {

    IsHomologousRepairDeficient() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean isHomologousRepairDeficient = record.molecular().isHomologousRepairDeficient();

        if (isHomologousRepairDeficient == null) {
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        } else if (isHomologousRepairDeficient) {
            return EvaluationFactory.create(EvaluationResult.PASS);
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
