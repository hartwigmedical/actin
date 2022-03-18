package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class IsHomologousRepairDeficient implements EvaluationFunction {

    IsHomologousRepairDeficient() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean isHomologousRepairDeficient = record.molecular().isHomologousRepairDeficient();

        if (isHomologousRepairDeficient == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No homologous repair deficiency status is known")
                    .build();
        }

        EvaluationResult result = isHomologousRepairDeficient ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor is homologous repair proficient");
            builder.addFailGeneralMessages("Molecular requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor is homologous repair deficient");
            builder.addPassGeneralMessages("Molecular requirements");
        }

        return builder.build();
    }
}
