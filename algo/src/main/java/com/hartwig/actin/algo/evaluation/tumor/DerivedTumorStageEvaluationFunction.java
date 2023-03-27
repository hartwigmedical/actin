package com.hartwig.actin.algo.evaluation.tumor;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

class DerivedTumorStageEvaluationFunction implements EvaluationFunction {

    private final TumorStageDerivationFunction tumorStageDerivationFunction;
    private final EvaluationFunction originalFunction;

    DerivedTumorStageEvaluationFunction(TumorStageDerivationFunction tumorStageDerivationFunction, EvaluationFunction originalFunction) {
        this.tumorStageDerivationFunction = tumorStageDerivationFunction;
        this.originalFunction = originalFunction;
    }

    @NotNull
    @Override
    public Evaluation evaluate(PatientRecord record) {
        if (record.clinical().tumor().stage() != null) {
            return originalFunction.evaluate(record);
        }
        Map<TumorStage, Evaluation> derivedResults =
                tumorStageDerivationFunction.apply(record.clinical().tumor()).collect(toMap(s -> s, s -> evaluatedDerivedStage(record, s)));
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

    private static boolean anyDerivedResultMatches(Map<TumorStage, Evaluation> derivedResults, EvaluationResult result) {
        return derivedResults.values().stream().anyMatch(e -> e.result().equals(result));
    }

    private static boolean allDerivedResultsMatch(Map<TumorStage, Evaluation> derivedResults, EvaluationResult result) {
        return derivedResults.values().stream().allMatch(e -> e.result().equals(result));
    }

    private Evaluation evaluatedDerivedStage(PatientRecord record, TumorStage newStage) {
        return originalFunction.evaluate(ImmutablePatientRecord.copyOf(record)
                .withClinical(ImmutableClinicalRecord.copyOf(record.clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(record.clinical().tumor()).withStage(newStage))));
    }
}
