package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import org.jetbrains.annotations.NotNull;

//TODO: Implement
public class GeneHasVariantInCodon implements EvaluationFunction {

    GeneHasVariantInCodon() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.NOT_IMPLEMENTED)
                .addPassSpecificMessages("Gene with variant in certain codon is not implemented yet")
                .build();
    }
}