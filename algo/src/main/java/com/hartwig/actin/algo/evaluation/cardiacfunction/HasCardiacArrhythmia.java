package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
            return Evaluation.FAIL;
        }

        return ecg.hasSigAberrationLatestECG() ? Evaluation.PASS : Evaluation.FAIL;
    }
}
