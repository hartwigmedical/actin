package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLimitedTumorMutationalLoad implements EvaluationFunction {

    private final int maxTumorMutationalLoad;

    public HasLimitedTumorMutationalLoad(final int maxTumorMutationalLoad) {
        this.maxTumorMutationalLoad = maxTumorMutationalLoad;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().tumorMutationalLoad() < maxTumorMutationalLoad) {
            return Evaluation.PASS;
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
