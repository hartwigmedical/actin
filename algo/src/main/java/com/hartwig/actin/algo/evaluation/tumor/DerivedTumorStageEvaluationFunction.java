package com.hartwig.actin.algo.evaluation.tumor;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Set;

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

        if (derivedResults.isEmpty()) {
            return originalFunction.evaluate(record);
        }

        if (derivedResults.size() == 1) {
            return follow(derivedResults);
        }

        if (allDerivedResultsMatch(derivedResults, EvaluationResult.PASS)) {
            return createEvaluationForDerivedResult(derivedResults, EvaluationResult.PASS);
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.PASS)) {
            return createEvaluationForDerivedResult(derivedResults, EvaluationResult.UNDETERMINED);
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.WARN)) {
            return createEvaluationForDerivedResult(derivedResults, EvaluationResult.WARN);
        } else if (allDerivedResultsMatch(derivedResults, EvaluationResult.NOT_EVALUATED)) {
            return createEvaluationForDerivedResult(derivedResults, EvaluationResult.NOT_EVALUATED);
        } else {
            return createEvaluationForDerivedResult(derivedResults, EvaluationResult.FAIL);
        }
    }

    private static Evaluation follow(Map<TumorStage, Evaluation> derivedResults) {
        Evaluation singleDerivedResult = derivedResults.values().iterator().next();
        return derivableResult(singleDerivedResult)
                ? createEvaluationForDerivedResult(derivedResults, singleDerivedResult.result())
                : singleDerivedResult;
    }

    private static boolean derivableResult(Evaluation singleDerivedResult) {
        return Set.of(EvaluationResult.PASS, EvaluationResult.FAIL, EvaluationResult.UNDETERMINED, EvaluationResult.WARN)
                .contains(singleDerivedResult.result());
    }

    static Evaluation createEvaluationForDerivedResult(Map<TumorStage, Evaluation> derived, EvaluationResult result) {
        switch (result) {
            case PASS:
                return DerivedTumorStageEvaluation.create(derived,
                        ImmutableEvaluation.Builder::addPassSpecificMessages,
                        ImmutableEvaluation.Builder::addPassGeneralMessages,
                        result);
            case UNDETERMINED:
                return DerivedTumorStageEvaluation.create(derived,
                        ImmutableEvaluation.Builder::addUndeterminedSpecificMessages,
                        ImmutableEvaluation.Builder::addUndeterminedGeneralMessages,
                        result);
            case WARN:
                return DerivedTumorStageEvaluation.create(derived,
                        ImmutableEvaluation.Builder::addWarnSpecificMessages,
                        ImmutableEvaluation.Builder::addWarnGeneralMessages,
                        result);
            case FAIL:
                return DerivedTumorStageEvaluation.create(derived,
                        ImmutableEvaluation.Builder::addFailSpecificMessages,
                        ImmutableEvaluation.Builder::addFailGeneralMessages,
                        result);
            default:
                throw new IllegalArgumentException();
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
