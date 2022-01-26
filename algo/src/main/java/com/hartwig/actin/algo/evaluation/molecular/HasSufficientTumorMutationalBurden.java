package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSufficientTumorMutationalBurden implements EvaluationFunction {

    private final double minTumorMutationalBurden;

    HasSufficientTumorMutationalBurden(final double minTumorMutationalBurden) {
        this.minTumorMutationalBurden = minTumorMutationalBurden;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double tumorMutationalBurden = record.molecular().tumorMutationalBurden();
        if (tumorMutationalBurden == null) {
            return Evaluation.UNDETERMINED;
        } else if (tumorMutationalBurden >= minTumorMutationalBurden) {
            return Evaluation.PASS;
        }

        return MolecularUtil.noMatchFound(record.molecular());
    }
}
