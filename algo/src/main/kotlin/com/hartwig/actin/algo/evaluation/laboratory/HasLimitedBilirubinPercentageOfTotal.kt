package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasLimitedBilirubinPercentageOfTotal implements LabEvaluationFunction {

    private final double maxPercentage;
    @NotNull
    private final LocalDate minValidDate;

    HasLimitedBilirubinPercentageOfTotal(final double maxPercentage, @NotNull final LocalDate minValidDate) {
        this.maxPercentage = maxPercentage;
        this.minValidDate = minValidDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        if (!labValue.code().equals(LabMeasurement.DIRECT_BILIRUBIN.code())) {
            throw new IllegalStateException("Bilirubin percentage must take direct bilirubin as input");
        }

        LabValue mostRecentTotal = interpretation.mostRecentValue(LabMeasurement.TOTAL_BILIRUBIN);
        if (mostRecentTotal == null || mostRecentTotal.date().isBefore(minValidDate)) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "No recent measurement found for total bilirubin, hence bilirubin percentage of total bilirubin could not be determined")
                    .addUndeterminedGeneralMessages("Bilirubin percentage of total bilirubin could not be determined")
                    .build();
        }

        boolean isPass = Double.compare(100 * (labValue.value() / mostRecentTotal.value()), maxPercentage) <= 0;

        EvaluationResult result = isPass ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(result);
        String messageStart = labValue.code() + " as percentage of " + mostRecentTotal.code();
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(messageStart + " exceeds maximum percentage of " + maxPercentage + "%");
            builder.addFailGeneralMessages(messageStart + " exceeding " + maxPercentage + "%");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(messageStart + " is below maximum percentage of " + maxPercentage + "%");
            builder.addPassGeneralMessages(messageStart + " below " + maxPercentage + "%");
        }

        return builder.build();
    }
}
