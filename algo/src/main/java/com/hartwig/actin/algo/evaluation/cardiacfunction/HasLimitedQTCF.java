package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ECG;

import org.jetbrains.annotations.NotNull;

public class HasLimitedQTCF implements EvaluationFunction {

    static final String EXPECTED_QTCF_UNIT = "ms";

    private final double maxQTCF;

    HasLimitedQTCF(final double maxQTCF) {
        this.maxQTCF = maxQTCF;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        ECG ecg = record.clinical().clinicalStatus().ecg();
        if (ecg == null || ecg.qtcfValue() == null || ecg.qtcfUnit() == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No measurement found for QTCF")
                    .build();
        }

        Integer value = ecg.qtcfValue();
        String unit = ecg.qtcfUnit();

        if (!unit.equalsIgnoreCase(EXPECTED_QTCF_UNIT)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("QTCF measure not in '" + EXPECTED_QTCF_UNIT + "': " + unit)
                    .build();
        }

        EvaluationResult result = Double.compare(value, maxQTCF) <= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("QTCF of " + value + " " + unit + " exceeds maximum threshold of " + maxQTCF);
            builder.addFailGeneralMessages("QTCF requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("QTCF of " + value + " " + unit + " is below maximum threshold of " + maxQTCF);
            builder.addPassGeneralMessages("QTCF requirements");
        }

        return builder.build();

    }
}
