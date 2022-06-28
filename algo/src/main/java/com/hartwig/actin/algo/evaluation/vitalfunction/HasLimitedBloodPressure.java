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

public class HasLimitedBloodPressure implements EvaluationFunction {

    @NotNull
    private final BloodPressureCategory category;
    private final double maxMedianBloodPressure;

    HasLimitedBloodPressure(@NotNull final BloodPressureCategory category, final double maxMedianBloodPressure) {
        this.category = category;
        this.maxMedianBloodPressure = maxMedianBloodPressure;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<VitalFunction> relevant = VitalFunctionSelector.selectBloodPressures(record.clinical().vitalFunctions(), category);
        String categoryDisplay = category.display().toLowerCase();

        if (relevant.isEmpty()) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No data found for " + categoryDisplay)
                    .build();
        }

        double median = VitalFunctionFunctions.determineMedianValue(relevant);
        EvaluationResult result = Double.compare(median, maxMedianBloodPressure) <= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        if (result == EvaluationResult.FAIL) {
            for (VitalFunction vitalFunction : relevant) {
                if (Double.compare(vitalFunction.value(), maxMedianBloodPressure) <= 0) {
                    return EvaluationFactory.recoverable()
                            .result(EvaluationResult.UNDETERMINED)
                            .addUndeterminedSpecificMessages(
                                    "Patient has median " + categoryDisplay + " blood pressure above " + maxMedianBloodPressure
                                            + " but also at least one measure below " + maxMedianBloodPressure)
                            .addUndeterminedGeneralMessages(categoryDisplay + " requirements")
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has median " + categoryDisplay + " exceeding " + maxMedianBloodPressure);
            builder.addFailGeneralMessages("High " + categoryDisplay);
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has median " + categoryDisplay + " below " + maxMedianBloodPressure);
            builder.addPassGeneralMessages(categoryDisplay + " requirements");
        }

        return builder.build();
    }
}

