package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.Optional;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator;
import com.hartwig.actin.util.ApplicationConfig;

import org.jetbrains.annotations.NotNull;

public class HasBMIUpToLimit implements EvaluationFunction {

    private static final String EXPECTED_UNIT = "kilogram";
    private static final double MIN_EXPECTED_HEIGHT_METRES = 1.5;
    private static final double MAX_EXPECTED_HEIGHT_METRES = 2.0;

    private final int maximumBMI;

    HasBMIUpToLimit(final int maximumBMI) {
        this.maximumBMI = maximumBMI;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Optional<BodyWeight> latestWeightOption = record.clinical()
                .bodyWeights()
                .stream()
                .filter(bodyWeight -> bodyWeight.unit().equalsIgnoreCase(EXPECTED_UNIT))
                .min(new BodyWeightDescendingDateComparator());

        if (latestWeightOption.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No body weights found in " + EXPECTED_UNIT)
                    .build();
        }
        BodyWeight latestWeight = latestWeightOption.get();

        double minimumRequiredHeight = calculateHeightForBmiAndWeight(maximumBMI, latestWeight.value());

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable();
        if (minimumRequiredHeight <= MIN_EXPECTED_HEIGHT_METRES) {
            builder.result(EvaluationResult.PASS)
                    .addPassSpecificMessages(String.format(ApplicationConfig.LOCALE,
                            "Patient weight %.1f kg will not exceed BMI limit of %d for height >= %.2f m",
                            latestWeight.value(),
                            maximumBMI,
                            minimumRequiredHeight))
                    .addPassGeneralMessages("BMI below limit");
        } else if (minimumRequiredHeight > MAX_EXPECTED_HEIGHT_METRES) {
            builder.result(EvaluationResult.FAIL)
                    .addFailSpecificMessages(String.format(ApplicationConfig.LOCALE,
                            "Patient weight %.1f kg will exceed BMI limit of %d for height < %.2f m",
                            latestWeight.value(),
                            maximumBMI,
                            minimumRequiredHeight))
                    .addFailGeneralMessages("BMI above limit");
        } else {
            builder.result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(String.format(ApplicationConfig.LOCALE,
                            "Patient weight %.1f kg will exceed BMI limit of %d for height < %.2f m",
                            latestWeight.value(),
                            maximumBMI,
                            minimumRequiredHeight))
                    .addWarnGeneralMessages("Potentially BMI above limit");
        }
        return builder.build();
    }

    private static double calculateHeightForBmiAndWeight(double bmi, double weight) {
        return Math.sqrt(weight / bmi);
    }
}
