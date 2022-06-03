package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ECG;

import org.jetbrains.annotations.NotNull;

public class HasCardiacArrhythmia implements EvaluationFunction {

    HasCardiacArrhythmia() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        ECG ecg = record.clinical().clinicalStatus().ecg();

        if (ecg == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("ECG details are missing, it is assumed there are no abnormalities")
                    .addFailGeneralMessages("Assumed no ECG abnormalities")
                    .build();
        }

        EvaluationResult result = ecg.hasSigAberrationLatestECG() ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("No known ECG abnormalities");
            builder.addFailGeneralMessages("No known ECG abnormalities");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Known ECG abnormalities: " + ecg.aberrationDescription());
            builder.addPassGeneralMessages("Known ECG abnormalities: " + ecg.aberrationDescription());
        }

        return builder.build();
    }
}
