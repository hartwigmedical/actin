package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Implement according to README
public class HasCancerWithNeuroendocrineComponent implements EvaluationFunction {

    HasCancerWithNeuroendocrineComponent() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently cannot be determined if cancer has neuroendocrine component")
                .addUndeterminedGeneralMessages("Undetermined neuroendocrine component")
                .build();
    }

}
