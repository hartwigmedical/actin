package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import org.jetbrains.annotations.NotNull;

//TODO: Implement
public class GeneHasUTR3Loss implements EvaluationFunction {

    GeneHasUTR3Loss() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.NOT_IMPLEMENTED)
                .addPassSpecificMessages("Gene UTR3 loss is not implemented yet")
                .build();
    }
}
