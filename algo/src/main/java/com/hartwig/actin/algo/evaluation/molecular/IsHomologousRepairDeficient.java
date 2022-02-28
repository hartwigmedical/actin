package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
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
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("No homologous repair deficiency status is known")
                    .build();
        }

        EvaluationResult result = isHomologousRepairDeficient ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Tumor is homologous repair proficient");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Tumor is homologous repair deficient");
        }

        return builder.build();
    }
}
