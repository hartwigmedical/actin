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

    static final String UNIT_TO_SELECT = "BPM";

    private static final int MAX_HEART_RATES_TO_USE = 5;

    private final double minMedianRestingHeartRate;
    private final double maxMedianRestingHeartRate;

    public HasRestingHeartRateWithinBounds(final double minMedianRestingHeartRate, final double maxMedianRestingHeartRate) {
        this.minMedianRestingHeartRate = minMedianRestingHeartRate;
        this.maxMedianRestingHeartRate = maxMedianRestingHeartRate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<VitalFunction> heartRates = VitalFunctionSelector.select(record.clinical().vitalFunctions(),
                VitalFunctionCategory.HEART_RATE,
                UNIT_TO_SELECT,
                MAX_HEART_RATES_TO_USE);

        if (heartRates.isEmpty()) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No heart rate data found")
                    .build();
        }

        double median = VitalFunctionFunctions.determineMedianValue(heartRates);

        EvaluationResult result =
                Double.compare(median, minMedianRestingHeartRate) >= 0 && Double.compare(median, maxMedianRestingHeartRate) <= 0
                        ? EvaluationResult.PASS
                        : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has does not have median heart rate between " + minMedianRestingHeartRate + " and "
                    + maxMedianRestingHeartRate);
            builder.addFailGeneralMessages("heart rate requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(
                    "Patient has median heart rate between " + minMedianRestingHeartRate + " and " + maxMedianRestingHeartRate);
            builder.addPassGeneralMessages("heart rate requirements");
        }

        return builder.build();
    }
}
