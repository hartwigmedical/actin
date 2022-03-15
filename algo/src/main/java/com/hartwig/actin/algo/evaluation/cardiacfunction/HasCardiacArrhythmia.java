package com.hartwig.actin.algo.evaluation.cardiacfunction;

import javax.annotation.Nullable;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ECG;

import org.jetbrains.annotations.NotNull;

public class HasCardiacArrhythmia implements EvaluationFunction {

    @Nullable
    private final String type;

    HasCardiacArrhythmia(@Nullable final String type) {
        this.type = type;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        ECG ecg = record.clinical().clinicalStatus().ecg();

        if (ecg == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("ECG details are missing, assumed there are none")
                    .build();
        }

        EvaluationResult result;
        if (ecg.hasSigAberrationLatestECG()) {
            result = (type == null || ecg.aberrationDescription().toLowerCase().contains(type.toLowerCase()))
                    ? EvaluationResult.PASS
                    : EvaluationResult.FAIL;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            if (type == null) {
                builder.addFailSpecificMessages("No known ECG abnormalities");
            } else {
                builder.addFailSpecificMessages("No known ECG abnormalities of type " + type);
            }
        } else if (result == EvaluationResult.PASS) {
            if (type == null) {
                builder.addPassSpecificMessages("Known ECG abnormalities: " + ecg.aberrationDescription());
            } else {
                builder.addPassSpecificMessages("Known ECG abnormalities of type " + type + ": " + ecg.aberrationDescription());
            }
        }

        return builder.build();
    }
}
