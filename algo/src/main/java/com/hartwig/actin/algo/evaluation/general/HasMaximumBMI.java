package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HasMaximumBMI implements EvaluationFunction {

    static final String EXPECTED_UNIT = "kilogram";
    static final double MIN_EXPECTED_HEIGHT_METRES = 1.5;
    static final double MAX_EXPECTED_HEIGHT_METRES = 2.0;

    private final int maximumBMI;

    HasMaximumBMI(final int maximumBMI) {
        this.maximumBMI = maximumBMI;
    }

    private double calculateWeightForBmiAndHeight(double bmi, double height) {
        return bmi * height * height;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Optional<BodyWeight> latestWeightOption = record.clinical().bodyWeights().stream()
                .filter(bodyWeight -> bodyWeight.unit().equalsIgnoreCase(EXPECTED_UNIT))
                .min(new BodyWeightDescendingDateComparator());

        if (latestWeightOption.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No body weights found in " + EXPECTED_UNIT)
                    .build();
        }
        BodyWeight latestWeight = latestWeightOption.get();

        double warnThresholdWeight = calculateWeightForBmiAndHeight(maximumBMI, MIN_EXPECTED_HEIGHT_METRES);
        double failThresholdWeight = calculateWeightForBmiAndHeight(maximumBMI, MAX_EXPECTED_HEIGHT_METRES);

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable();
        if (latestWeight.value() <= warnThresholdWeight) {
            builder.result(EvaluationResult.PASS)
                    .addPassSpecificMessages(String.format("Patient weight %.1f kg will not exceed BMI limit of %d for height >= %.1f m",
                            latestWeight.value(), maximumBMI, MIN_EXPECTED_HEIGHT_METRES))
                    .addPassGeneralMessages("Acceptable BMI");
        } else if (latestWeight.value() > failThresholdWeight) {
            builder.result(EvaluationResult.FAIL)
                    .addFailSpecificMessages(String.format("Patient weight %.1f kg will exceed BMI limit of %d for height <= %.1f m",
                            latestWeight.value(), maximumBMI, MAX_EXPECTED_HEIGHT_METRES))
                    .addFailGeneralMessages("Excessive BMI");
        } else {
            builder.result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(String.format("Patient weight %.1f kg will exceed BMI limit of %d for " +
                                    "some heights between %.1f and %.1f m",
                            latestWeight.value(), maximumBMI, MIN_EXPECTED_HEIGHT_METRES, MAX_EXPECTED_HEIGHT_METRES))
                    .addWarnGeneralMessages("Potentially high BMI");
        }
        return builder.build();
    }
}
