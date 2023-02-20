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

public class HasSufficientPulseOximetry implements EvaluationFunction {

    private static final int MAX_PULSE_OXIMETRY_TO_USE = 5;

    private final double minMedianPulseOximetry;

    HasSufficientPulseOximetry(final double minMedianPulseOximetry) {
        this.minMedianPulseOximetry = minMedianPulseOximetry;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<VitalFunction> pulseOximetries = VitalFunctionSelector.select(record.clinical().vitalFunctions(),
                VitalFunctionCategory.SPO2,
                null,
                MAX_PULSE_OXIMETRY_TO_USE);

        if (pulseOximetries.isEmpty()) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No pulse oximetries readouts found")
                    .build();
        }

        double median = VitalFunctionFunctions.determineMedianValue(pulseOximetries);
        EvaluationResult result = Double.compare(median, minMedianPulseOximetry) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        if (result == EvaluationResult.FAIL) {
            for (VitalFunction pulseOximetry : pulseOximetries) {
                if (Double.compare(pulseOximetry.value(), minMedianPulseOximetry) >= 0) {
                    return EvaluationFactory.recoverable()
                            .result(EvaluationResult.UNDETERMINED)
                            .addUndeterminedSpecificMessages("Patient has median pulse oximetry below " + minMedianPulseOximetry
                                    + " but also at least one measure above " + minMedianPulseOximetry)
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has median pulse oximetry below " + minMedianPulseOximetry);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has median pulse oximetry exceeding " + minMedianPulseOximetry);
        }

        return builder.build();
    }
}
