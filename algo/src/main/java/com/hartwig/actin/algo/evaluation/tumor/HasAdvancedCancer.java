package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class HasAdvancedCancer implements EvaluationFunction {

    private static final Set<TumorStage> STAGES_CONSIDERED_ADVANCED = Sets.newHashSet();

    static {
        STAGES_CONSIDERED_ADVANCED.add(TumorStage.III);
        STAGES_CONSIDERED_ADVANCED.add(TumorStage.IIIA);
        STAGES_CONSIDERED_ADVANCED.add(TumorStage.IIIB);
        STAGES_CONSIDERED_ADVANCED.add(TumorStage.IIIC);
        STAGES_CONSIDERED_ADVANCED.add(TumorStage.IV);
    }

    HasAdvancedCancer() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        TumorStage stage = record.clinical().tumor().stage();

        if (stage == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Tumor stage details are missing")
                    .build();
        }

        EvaluationResult result = STAGES_CONSIDERED_ADVANCED.contains(stage) ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor stage " + stage + " is not considered advanced (III/IV)");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor stage " + stage + " is considered advanced (III/IV)");
        }

        return builder.build();
    }
}
