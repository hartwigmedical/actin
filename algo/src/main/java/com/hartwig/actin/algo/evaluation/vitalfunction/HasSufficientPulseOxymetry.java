package com.hartwig.actin.algo.evaluation.vitalfunction;

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
import com.hartwig.actin.clinical.sort.VitalFunctionDescendingDateComparator;

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
        List<VitalFunction> pulseOxymetries = selectPulseOxymetries(record.clinical().vitalFunctions());

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

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has average pulse oxymetry below " + minAvgPulseOxymetry);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has average pulse oxymetry exceeding " + minAvgPulseOxymetry);
        }

        return builder.build();
    }

    @NotNull
    private static List<VitalFunction> selectPulseOxymetries(@NotNull List<VitalFunction> vitalFunctions) {
        List<VitalFunction> filtered = Lists.newArrayList();
        for (VitalFunction vitalFunction : vitalFunctions) {
            if (vitalFunction.category() == VitalFunctionCategory.SPO2) {
                filtered.add(vitalFunction);
            }
        }

        filtered.sort(new VitalFunctionDescendingDateComparator());

        return filtered.subList(0, Math.min(filtered.size(), MAX_PULSE_OXYMETRY_TO_USE));
    }
}
