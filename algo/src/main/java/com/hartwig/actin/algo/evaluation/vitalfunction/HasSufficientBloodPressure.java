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

public class HasSufficientBloodPressure implements EvaluationFunction {

    @NotNull
    private final BloodPressureCategory category;
    private final double minAvgBloodPressure;

    HasSufficientBloodPressure(@NotNull final BloodPressureCategory category, final double minAvgBloodPressure) {
        this.category = category;
        this.minAvgBloodPressure = minAvgBloodPressure;
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

        EvaluationResult result = Double.compare(avg, minAvgBloodPressure) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        if (result == EvaluationResult.FAIL) {
            for (VitalFunction vitalFunction : relevant) {
                if (Double.compare(vitalFunction.value(), minAvgBloodPressure) >= 0) {
                    return EvaluationFactory.recoverable()
                            .result(EvaluationResult.UNDETERMINED)
                            .addUndeterminedSpecificMessages(
                                    "Patient has average " + categoryDisplay + " blood pressure below " + minAvgBloodPressure
                                            + " but also at least one measure above " + minAvgBloodPressure)
                            .addUndeterminedGeneralMessages(categoryDisplay + " requirements")
                            .build();
                }
            }
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has average " + categoryDisplay + " blood pressure below " + minAvgBloodPressure);
            builder.addFailGeneralMessages(categoryDisplay + " requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has average " + categoryDisplay + " blood pressure exceeding " + minAvgBloodPressure);
            builder.addPassGeneralMessages(categoryDisplay + " requirements");
        }

        return builder.build();
    }
}
