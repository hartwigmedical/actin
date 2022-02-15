package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class TumorBiopsyTakenBeforeInformedConsent implements EvaluationFunction {

    TumorBiopsyTakenBeforeInformedConsent() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // Should only be pass when WGS is available, but this is currently mandatory in ACTIN.
        return EvaluationFactory.create(EvaluationResult.PASS);
    }
}
