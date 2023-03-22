package com.hartwig.actin.algo.evaluation.tumor;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class DerivedTumorStageEvaluationFunction implements EvaluationFunction {

    private final TumorStageDerivationFunction tumorStageDerivationFunction;
    private final EvaluationFunction originalFunction;

    public DerivedTumorStageEvaluationFunction(final TumorStageDerivationFunction tumorStageDerivationFunction,
            final EvaluationFunction originalFunction) {
        this.tumorStageDerivationFunction = tumorStageDerivationFunction;
        this.originalFunction = originalFunction;
    }

    @NotNull
    @Override
    public Evaluation evaluate(final PatientRecord record) {
        if (record.clinical().tumor().stage() != null) {
            return originalFunction.evaluate(record);
        }
        Set<TumorStage> derivedStages = tumorStageDerivationFunction.apply(record.clinical().tumor());
        Map<TumorStage, Evaluation> derivedResults = derivedStages.stream().collect(toMap(s -> s, s -> evaluatedDerivedStage(record, s)));
        if (allDerivedResultsMatch(derivedResults, EvaluationResult.PASS)) {
            return combineEvaluationResults(derivedResults, accumulateAllMessagesInUndermined(), EvaluationResult.PASS);
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.PASS)) {
            return combineEvaluationResults(derivedResults, accumulateAllMessagesInUndermined(), EvaluationResult.UNDETERMINED);
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.WARN)) {
            return combineEvaluationResults(derivedResults, accumulateAllMessagesInUndermined(), EvaluationResult.WARN);
        } else {
            return combineEvaluationResults(derivedResults, accumulateAllMessagesInUndermined(), EvaluationResult.FAIL);
        }
    }

    @NotNull
    private static List<String> withDerivedSuffix(final TumorStage key, final Set<String> messages) {
        return messages.stream().map(m -> format("%s [Implied by derived tumor stage %s]", m, key)).collect(toList());
    }

    private static boolean anyDerivedResultMatches(final Map<TumorStage, Evaluation> derivedResults, final EvaluationResult warn) {
        return derivedResults.values().stream().anyMatch(e -> e.result().equals(warn));
    }

    private static boolean allDerivedResultsMatch(final Map<TumorStage, Evaluation> derivedResults, final EvaluationResult result) {
        return derivedResults.values().stream().allMatch(e -> e.result().equals(result));
    }

    private static ImmutableEvaluation combineEvaluationResults(final Map<TumorStage, Evaluation> derivedResults,
            final BiConsumer<ImmutableEvaluation.Builder, Map.Entry<TumorStage, Evaluation>> messageAccumulator,
            final EvaluationResult pass) {
        return derivedResults.entrySet().stream().collect(toEvaluationBuilder(messageAccumulator)).result(pass).recoverable(false).build();
    }

    private static BiConsumer<ImmutableEvaluation.Builder, Map.Entry<TumorStage, Evaluation>> accumulateAllMessagesInUndetermined() {
        return (b, e) -> b.addAllUndeterminedGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().passGeneralMessages()))
                .addAllUndeterminedGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().failGeneralMessages()))
                .addAllUndeterminedGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().undeterminedGeneralMessages()))
                .addAllUndeterminedSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().passSpecificMessages()))
                .addAllUndeterminedSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().failSpecificMessages()))
                .addAllUndeterminedSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().undeterminedSpecificMessages()));
    }

    private static BiConsumer<ImmutableEvaluation.Builder, Map.Entry<TumorStage, Evaluation>> accumulateAllMessagesInWarn() {
        return (b, e) -> b.addAllWarnGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().passGeneralMessages()))
                .addAllWarnGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().failGeneralMessages()))
                .addAllWarnGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().undeterminedGeneralMessages()))
                .addAllWarnSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().passSpecificMessages()))
                .addAllWarnSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().failSpecificMessages()))
                .addAllWarnSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().undeterminedSpecificMessages()));
    }

    private static BiConsumer<ImmutableEvaluation.Builder, Map.Entry<TumorStage, Evaluation>> accumulateAllMessagesInFail() {
        return (b, e) -> b.addAllFailGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().passGeneralMessages()))
                .addAllFailGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().failGeneralMessages()))
                .addAllFailGeneralMessages(withDerivedSuffix(e.getKey(), e.getValue().undeterminedGeneralMessages()))
                .addAllFailSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().passSpecificMessages()))
                .addAllFailSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().failSpecificMessages()))
                .addAllFailSpecificMessages(withDerivedSuffix(e.getKey(), e.getValue().undeterminedSpecificMessages()));
    }

    private static Collector<Map.Entry<TumorStage, Evaluation>, ImmutableEvaluation.Builder, ImmutableEvaluation.Builder> toEvaluationBuilder(
            final BiConsumer<ImmutableEvaluation.Builder, Map.Entry<TumorStage, Evaluation>> messageAccumulator) {
        return Collector.of(ImmutableEvaluation::builder, messageAccumulator, (b1, b2) -> b1.from(b2.build()));
    }

    private Evaluation evaluatedDerivedStage(final @NotNull PatientRecord record, final TumorStage newStage) {
        return originalFunction.evaluate(ImmutablePatientRecord.copyOf(record)
                .withClinical(ImmutableClinicalRecord.copyOf(record.clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(record.clinical().tumor()).withStage(newStage))));
    }
}
