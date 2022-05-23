package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.jetbrains.annotations.NotNull;

public class HasSufficientPulseOxymetry implements EvaluationFunction {

    private static final int MAX_PULSE_OXYMETRY_TO_USE = 5;

    private final double minMedianPulseOxymetry;

    HasSufficientPulseOxymetry(final double minMedianPulseOxymetry) {
        this.minMedianPulseOxymetry = minMedianPulseOxymetry;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<VitalFunction> pulseOxymetries = VitalFunctionSelector.select(record.clinical().vitalFunctions(),
                VitalFunctionCategory.SPO2,
                null,
                MAX_PULSE_OXYMETRY_TO_USE);

        if (pulseOxymetries.isEmpty()) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No pulse oximetries readouts found")
                    .build();
        }

        List<Double> values = Lists.newArrayList();
        for (VitalFunction pulseOxymetry : pulseOxymetries) {
            values.add(pulseOxymetry.value());
        }
        values.sort(Comparator.naturalOrder());

        double median;
        int index = (int) Math.ceil(values.size() / 2D) - 1;
        if (values.size() % 2 == 0) {
            median = 0.5 * (values.get(index) + values.get(index + 1));
        } else {
            median = values.get(index);
        }

        EvaluationResult result = Double.compare(median, minMedianPulseOxymetry) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        if (result == EvaluationResult.FAIL) {
            for (VitalFunction pulseOxymetry : pulseOxymetries) {
                if (Double.compare(pulseOxymetry.value(), minMedianPulseOxymetry) >= 0) {
                    return EvaluationFactory.recoverable()
                            .result(EvaluationResult.UNDETERMINED)
                            .addUndeterminedSpecificMessages("Patient has median pulse oximetry below " + minMedianPulseOxymetry
                                    + " but also at least one measure above " + minMedianPulseOxymetry)
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has median pulse oximetry below " + minMedianPulseOxymetry);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has median pulse oximetry exceeding " + minMedianPulseOxymetry);
        }

        return builder.build();
    }
}
