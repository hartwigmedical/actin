package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
            return Evaluation.UNDETERMINED;
        }

        return STAGES_CONSIDERED_ADVANCED.contains(stage) ? Evaluation.PASS : Evaluation.FAIL;
    }
}
