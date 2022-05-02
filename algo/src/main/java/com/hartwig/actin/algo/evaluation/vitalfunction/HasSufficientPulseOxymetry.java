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

public class HasSufficientPulseOxymetry implements EvaluationFunction {

    private static final int MAX_PULSE_OXYMETRY_TO_USE = 5;

    private final double minAvgPulseOxymetry;

    HasSufficientPulseOxymetry(final double minAvgPulseOxymetry) {
        this.minAvgPulseOxymetry = minAvgPulseOxymetry;
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
                    .addUndeterminedSpecificMessages("No pulse oxymetries readouts found")
                    .build();
        }

        double sum = 0;
        for (VitalFunction pulseOxymetry : pulseOxymetries) {
            sum += pulseOxymetry.value();
        }

        double avg = sum / pulseOxymetries.size();

        EvaluationResult result = Double.compare(avg, minAvgPulseOxymetry) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        if (result == EvaluationResult.FAIL) {
            for (VitalFunction pulseOxymetry : pulseOxymetries) {
                if (Double.compare(pulseOxymetry.value(), minAvgPulseOxymetry) >= 0) {
                    return EvaluationFactory.recoverable()
                            .result(EvaluationResult.UNDETERMINED)
                            .addUndeterminedSpecificMessages("Patient has average pulse oxymetry below " + minAvgPulseOxymetry
                                    + " but also at least one measure above " + minAvgPulseOxymetry)
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has average pulse oxymetry below " + minAvgPulseOxymetry);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has average pulse oxymetry exceeding " + minAvgPulseOxymetry);
        }

        return builder.build();
    }
}
