package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.jetbrains.annotations.NotNull;

public class HasRestingHeartRateWithinBounds implements EvaluationFunction {

    private static final int MAX_HEART_RATES_TO_USE = 5;
    private static final String UNIT_TO_SELECT = "BPM";

    private final double minAvgRestingHeartRate;
    private final double maxAvgRestingHeartRate;

    public HasRestingHeartRateWithinBounds(final double minAvgRestingHeartRate, final double maxAvgRestingHeartRate) {
        this.minAvgRestingHeartRate = minAvgRestingHeartRate;
        this.maxAvgRestingHeartRate = maxAvgRestingHeartRate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<VitalFunction> relevant = VitalFunctionSelector.select(record.clinical().vitalFunctions(),
                VitalFunctionCategory.HEART_RATE,
                UNIT_TO_SELECT,
                MAX_HEART_RATES_TO_USE);

        if (relevant.isEmpty()) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No heart rate data found")
                    .build();
        }

        double sum = 0;
        for (VitalFunction vitalFunction : relevant) {
            sum += vitalFunction.value();
        }

        double avg = sum / relevant.size();

        EvaluationResult result = Double.compare(avg, minAvgRestingHeartRate) >= 0 && Double.compare(avg, maxAvgRestingHeartRate) <= 0
                ? EvaluationResult.PASS
                : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(
                    "Patient has does not have average heart rate between " + minAvgRestingHeartRate + " and " + maxAvgRestingHeartRate);
            builder.addFailGeneralMessages("heart rate requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(
                    "Patient has average heart rate between " + minAvgRestingHeartRate + " and " + maxAvgRestingHeartRate);
            builder.addPassGeneralMessages("heart rate requirements");
        }

        return builder.build();
    }
}
