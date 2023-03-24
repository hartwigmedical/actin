package com.hartwig.actin.algo.evaluation.tumor;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Set;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class DerivedTumorStageEvaluationFunction implements EvaluationFunction {

    private final TumorStageDerivationFunction tumorStageDerivationFunction;
    private final EvaluationFunction originalFunction;

    public DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction tumorStageDerivationFunction,
            EvaluationFunction originalFunction) {
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

        if (derivedResults.size() == 1) {
            return DerivedTumorStageEvaluationFactory.follow(derivedResults.entrySet().iterator().next());
        }
        if (allDerivedResultsMatch(derivedResults, EvaluationResult.PASS)) {
            return DerivedTumorStageEvaluationFactory.pass(derivedResults);
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.PASS)) {
            return DerivedTumorStageEvaluationFactory.undetermined(derivedResults);
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.WARN)) {
            return DerivedTumorStageEvaluationFactory.warn(derivedResults);
        } else {
            return DerivedTumorStageEvaluationFactory.fail(derivedResults);
        }
    }

    private static boolean anyDerivedResultMatches(final Map<TumorStage, Evaluation> derivedResults, final EvaluationResult warn) {
        return derivedResults.values().stream().anyMatch(e -> e.result().equals(warn));
    }

    private static boolean allDerivedResultsMatch(final Map<TumorStage, Evaluation> derivedResults, final EvaluationResult result) {
        return derivedResults.values().stream().allMatch(e -> e.result().equals(result));
    }

    private Evaluation evaluatedDerivedStage(final @NotNull PatientRecord record, final TumorStage newStage) {
        return originalFunction.evaluate(ImmutablePatientRecord.copyOf(record)
                .withClinical(ImmutableClinicalRecord.copyOf(record.clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(record.clinical().tumor()).withStage(newStage))));
    }
}
