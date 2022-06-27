package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;

import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
public class HasLimitedBloodPressure implements EvaluationFunction {

    @NotNull
    private final BloodPressureCategory category;
    private final double maxAvgBloodPressure;

    HasLimitedBloodPressure(@NotNull final BloodPressureCategory category, final double maxAvgBloodPressure) {
        this.category = category;
        this.maxAvgBloodPressure = maxAvgBloodPressure;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<VitalFunction> relevant = VitalFunctionSelector.selectRelevant(record.clinical().vitalFunctions(), category);
        String categoryDisplay = category.display().toLowerCase();

        if (relevant.isEmpty()) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No data found for " + categoryDisplay)
                    .build();
        }

        double sum = 0;
        for (VitalFunction vitalFunction : relevant) {
            sum += vitalFunction.value();
        }

        double avg = sum / relevant.size();

        EvaluationResult result = Double.compare(avg, maxAvgBloodPressure) <= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        if (result == EvaluationResult.FAIL) {
            for (VitalFunction vitalFunction : relevant) {
                if (Double.compare(vitalFunction.value(), maxAvgBloodPressure) <= 0) {
                    return EvaluationFactory.recoverable()
                            .result(EvaluationResult.UNDETERMINED)
                            .addUndeterminedSpecificMessages(
                                    "Patient has average " + categoryDisplay + " blood pressure above " + maxAvgBloodPressure
                                            + " but also at least one measure below " + maxAvgBloodPressure)
                            .addUndeterminedGeneralMessages(categoryDisplay + " requirements")
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has average " + categoryDisplay + " exceeding " + maxAvgBloodPressure);
            builder.addFailGeneralMessages("High " + categoryDisplay);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has average " + categoryDisplay + " below " + maxAvgBloodPressure);
            builder.addPassGeneralMessages(categoryDisplay + " requirements");
        }

        return builder.build();
    }
}

