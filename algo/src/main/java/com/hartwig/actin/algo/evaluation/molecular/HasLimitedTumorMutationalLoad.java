package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLimitedTumorMutationalLoad implements EvaluationFunction {

    private final int maxTumorMutationalLoad;

    public HasLimitedTumorMutationalLoad(final int maxTumorMutationalLoad) {
        this.maxTumorMutationalLoad = maxTumorMutationalLoad;
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        Integer tumorMutationalLoad = record.molecular().tumorMutationalLoad();
        if (tumorMutationalLoad == null) {
            return EvaluationResult.UNDETERMINED;
        } else if (tumorMutationalLoad <= maxTumorMutationalLoad) {
            return EvaluationResult.PASS;
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
