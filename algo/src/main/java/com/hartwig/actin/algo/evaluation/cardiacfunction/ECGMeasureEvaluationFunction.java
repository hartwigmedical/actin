package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static java.lang.String.format;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ECGMeasure;

import org.jetbrains.annotations.NotNull;

class ECGMeasureEvaluationFunction implements EvaluationFunction {

    enum ThresholdCriteria {
        MAXIMUM(Comparator.comparingDouble(Number::doubleValue).reversed(),
                "%s of %s %s does not exceed minimum threshold of %s",
                "%s of %s %s is above or equal to minimum threshold of %s"),
        MINIMUM(Comparator.comparingDouble(Number::doubleValue),
                "%s of %s %s exceeds maximum threshold of %s",
                "%s of %s %s is below or equal to maximum threshold of %s");
        private final Comparator<Number> comparator;
        private final String failMessageTemplate;
        private final String passMessageTemplate;

        ThresholdCriteria(final Comparator<Number> comparator, final String failMessageTemplate, final String passMessageTemplate) {
            this.comparator = comparator;
            this.failMessageTemplate = failMessageTemplate;
            this.passMessageTemplate = passMessageTemplate;
        }
    }

    private final double threshold;
    private final ECGMeasureName measureName;
    private final ECGUnit expectedUnit;
    private final Function<ECG, Optional<ECGMeasure>> extractingECGMeasure;
    private final ThresholdCriteria thresholdCriteria;

    public ECGMeasureEvaluationFunction(final ECGMeasureName measureName, final double threshold, final ECGUnit expectedUnit,
            final Function<ECG, Optional<ECGMeasure>> extractingECGMeasure, final ThresholdCriteria thresholdCriteria) {
        this.threshold = threshold;
        this.measureName = measureName;
        this.expectedUnit = expectedUnit;
        this.extractingECGMeasure = extractingECGMeasure;
        this.thresholdCriteria = thresholdCriteria;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return Optional.ofNullable(record.clinical().clinicalStatus().ecg())
                .flatMap(extractingECGMeasure)
                .map(this::evaluate)
                .orElse(EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(format("No %s known", measureName))
                        .addUndeterminedGeneralMessages(format("Undetermined %s", measureName))
                        .build());
    }

    @NotNull
    private Evaluation evaluate(final ECGMeasure measure) {
        if (!measure.unit().equals(expectedUnit.getSymbol())) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("%s measure not in '%s': %s",
                            measureName.name(),
                            expectedUnit.getSymbol(),
                            measure.unit())
                    .addUndeterminedGeneralMessages(format("Unrecognized unit of %s evaluation", measureName))
                    .build();
        }
        EvaluationResult result =
                thresholdCriteria.comparator.compare(measure.value(), threshold) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(format(thresholdCriteria.failMessageTemplate,
                    measureName,
                    measure.value(),
                    measure.unit(),
                    threshold)).addFailGeneralMessages(generalMessage(measureName.name()));
        } else {
            builder.addPassSpecificMessages(format(thresholdCriteria.passMessageTemplate,
                    measureName,
                    measure.value(),
                    measure.unit(),
                    threshold)).addPassGeneralMessages(generalMessage(measureName.name()));
        }
        return builder.build();
    }

    private static String generalMessage(final String measureName) {
        return format("%s requirements", measureName);
    }

    static ECGMeasureEvaluationFunction hasLimitedQTCF(final double maxQTCF) {
        return new ECGMeasureEvaluationFunction(ECGMeasureName.QTCF,
                maxQTCF,
                ECGUnit.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ECGMeasureEvaluationFunction.ThresholdCriteria.MAXIMUM);
    }

    static ECGMeasureEvaluationFunction hasSufficientQTCF(final double minQTCF) {
        return new ECGMeasureEvaluationFunction(ECGMeasureName.QTCF,
                minQTCF,
                ECGUnit.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ThresholdCriteria.MINIMUM);
    }

    static ECGMeasureEvaluationFunction hasSufficientJTc(final double maxQTCF) {
        return new ECGMeasureEvaluationFunction(ECGMeasureName.JTC,
                maxQTCF,
                ECGUnit.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.jtcMeasure()),
                ThresholdCriteria.MINIMUM);
    }
}
