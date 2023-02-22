package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TumorMetastasisEvaluator {

    @NotNull
    public static Evaluation evaluate(@Nullable Boolean hasMetastases, @NotNull String metastasisType) {
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable();
        if (hasMetastases == null) {
            builder.result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(String.format("Data regarding presence of %s metastases is missing",
                            metastasisType))
                    .addUndeterminedGeneralMessages(String.format("Missing %s metastasis data", metastasisType));
        } else if (hasMetastases) {
            String capitalizedType = metastasisType.substring(0, 1).toUpperCase() + metastasisType.substring(1);
            builder.result(EvaluationResult.PASS)
                    .addPassSpecificMessages(String.format("%s metastases are present", capitalizedType))
                    .addPassGeneralMessages(String.format("%s metastases", capitalizedType));
        } else {
            builder.result(EvaluationResult.FAIL)
                    .addFailSpecificMessages(String.format("No %s metastases present", metastasisType))
                    .addFailGeneralMessages(String.format("No %s metastases", metastasisType));
        }
        return builder.build();
    }
}
