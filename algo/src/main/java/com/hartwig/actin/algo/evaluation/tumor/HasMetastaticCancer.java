package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        TumorStage stage = record.clinical().tumor().stage();

        if (stage == null) {
            return EvaluationResult.UNDETERMINED;
        }

        return STAGES_CONSIDERED_METASTATIC.contains(stage) ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }
}
