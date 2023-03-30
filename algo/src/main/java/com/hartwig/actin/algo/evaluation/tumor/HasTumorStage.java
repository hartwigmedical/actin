package com.hartwig.actin.algo.evaluation.tumor;

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;

public class HasTumorStage implements EvaluationFunction {

    private final TumorStageDerivationFunction tumorStageDerivationFunction;
    private final TumorStage stageToMatch;

    HasTumorStage(final TumorStageDerivationFunction tumorStageDerivationFunction, final TumorStage stageToMatch) {
        this.tumorStageDerivationFunction = tumorStageDerivationFunction;
        this.stageToMatch = stageToMatch;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        TumorStage stage = record.clinical().tumor().stage();
        if (stage == null) {
            Set<TumorStage> derivedStages = tumorStageDerivationFunction.apply(record.clinical().tumor()).collect(toSet());
            if (derivedStages.size() == 1) {
                return evaluateWithStage(derivedStages.iterator().next());
            } else if (derivedStages.stream().map(this::evaluateWithStage).anyMatch(e -> e.result().equals(EvaluationResult.PASS))) {
                return EvaluationFactory.undetermined("No tumor stage details present, but multiple possible derived are possible",
                        "Missing tumor stage details");
            } else {
                return EvaluationFactory.fail("Tumor stage details are missing", "Missing tumor stage details");
            }
        }

        return evaluateWithStage(stage);
    }

    private ImmutableEvaluation evaluateWithStage(TumorStage stage) {
        boolean hasTumorStage = stage == stageToMatch || stage.category() == stageToMatch;

        EvaluationResult result = hasTumorStage ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient tumor stage is not exact stage " + stageToMatch.display());
            builder.addFailGeneralMessages("Inadequate tumor stage");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient tumor stage is exact stage " + stageToMatch.display());
            builder.addPassGeneralMessages("Adequate tumor stage");
        }

        return builder.build();
    }
}
