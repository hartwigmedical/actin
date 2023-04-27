package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasLymphNodeMetastases implements EvaluationFunction {

    HasLymphNodeMetastases() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return TumorMetastasisEvaluator.evaluate(record.clinical().tumor().hasLymphNodeLesions(), "lymph node");
    }
}
