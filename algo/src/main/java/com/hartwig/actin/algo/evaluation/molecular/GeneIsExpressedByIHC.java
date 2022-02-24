package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;

import org.jetbrains.annotations.NotNull;

public class GeneIsExpressedByIHC implements EvaluationFunction {

    GeneIsExpressedByIHC() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // Currently assume we do not have IHC results available
        return EvaluationFactory.create(EvaluationResult.FAIL);
    }
}