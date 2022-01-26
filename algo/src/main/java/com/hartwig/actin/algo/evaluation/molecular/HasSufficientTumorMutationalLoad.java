package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSufficientTumorMutationalLoad implements EvaluationFunction {

    private final int minTumorMutationalLoad;

    public HasSufficientTumorMutationalLoad(final int minTumorMutationalLoad) {
        this.minTumorMutationalLoad = minTumorMutationalLoad;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer tumorMutationalLoad = record.molecular().tumorMutationalLoad();
        if (tumorMutationalLoad == null) {
            return Evaluation.UNDETERMINED;
        } else if (tumorMutationalLoad >= minTumorMutationalLoad) {
            return Evaluation.PASS;
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
