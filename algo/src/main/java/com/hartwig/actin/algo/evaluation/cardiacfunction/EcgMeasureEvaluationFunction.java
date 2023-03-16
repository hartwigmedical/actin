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

public class EcgMeasureEvaluationFunction implements EvaluationFunction {

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

        ThresholdCriteria(final Comparator<Number> comparator, final String failMessageTemplate,
                final String passMessageTemplate) {
            this.comparator = comparator;
            this.failMessageTemplate = failMessageTemplate;
            this.passMessageTemplate = passMessageTemplate;
        }
    }

    private final double threshold;
    private final String measureName;
    private final String expectedUnit;
    private final Function<ECG, Optional<ECGMeasure>> extractingECG;
    private final ThresholdCriteria thresholdCriteria;

    public EcgMeasureEvaluationFunction(final String measureName, final double threshold, final String expectedUnit,
            final Function<ECG, Optional<ECGMeasure>> extractingECG, final ThresholdCriteria thresholdCriteria) {
        this.threshold = threshold;
        this.measureName = measureName;
        this.expectedUnit = expectedUnit;
        this.extractingECG = extractingECG;
        this.thresholdCriteria = thresholdCriteria;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return Optional.ofNullable(record.clinical().clinicalStatus().ecg())
                .flatMap(extractingECG)
                .map(evaluate())
                .orElse(EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(format("No %s known", measureName))
                        .addUndeterminedGeneralMessages(format("Undetermined %s", measureName))
                        .build());
    }

    @NotNull
    private Function<ECGMeasure, ImmutableEvaluation> evaluate() {
        return m -> {
            if (!m.unit().equals(expectedUnit)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages("%s measure not in '%s': %s",
                                measureName,
                                expectedUnit,
                                m.unit())
                        .addUndeterminedGeneralMessages(format("Unrecognized unit of %s evaluation", measureName))
                        .build();
            }
            EvaluationResult result = thresholdCriteria.comparator.compare(m.value(), threshold) >= 0
                    ? EvaluationResult.PASS
                    : EvaluationResult.FAIL;
            ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
            if (result == EvaluationResult.FAIL) {
                builder.addFailSpecificMessages(format(thresholdCriteria.failMessageTemplate,
                        measureName,
                        m.value(),
                        m.unit(),
                        threshold)).addFailGeneralMessages(generalMessage(measureName));
            } else {
                builder.addPassSpecificMessages(format(thresholdCriteria.passMessageTemplate,
                        measureName,
                        m.value(),
                        m.unit(),
                        threshold)).addPassGeneralMessages(generalMessage(measureName));
            }
            return builder.build();
        };
    }

    private static String generalMessage(final String measureName) {
        return format("%s requirements", measureName);
    }
}
