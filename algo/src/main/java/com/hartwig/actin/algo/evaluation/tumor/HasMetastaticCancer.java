package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class HasMetastaticCancer implements EvaluationFunction {

    private static final Set<TumorStage> STAGES_CONSIDERED_METASTATIC = Sets.newHashSet();

    static {
        STAGES_CONSIDERED_METASTATIC.add(TumorStage.IV);
    }

    HasMetastaticCancer() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        TumorStage stage = record.clinical().tumor().stage();

        if (stage == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Tumor stage details are missing")
                    .build();
        }

        EvaluationResult result = STAGES_CONSIDERED_METASTATIC.contains(stage) ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor stage " + stage.display() + " is not considered metastatic (IV)");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor stage " + stage.display() + " is considered metastatic (IV)");
        }

        return builder.build();
    }
}
