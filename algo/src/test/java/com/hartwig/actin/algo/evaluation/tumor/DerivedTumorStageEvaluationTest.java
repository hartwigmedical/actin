package com.hartwig.actin.algo.evaluation.tumor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationAssert;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Test;

public class DerivedTumorStageEvaluationTest {

    @Test
    public void shouldUseMessageFromWorstOutcomeAlongWithDerivationNote() {
        Evaluation evaluation = DerivedTumorStageEvaluation.create(Map.of(TumorStage.I,
                        EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.PASS)
                                .addPassSpecificMessages("Pass specific message")
                                .addPassGeneralMessages("Pass general message")
                                .build(),
                        TumorStage.II,
                        EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.UNDETERMINED)
                                .addPassSpecificMessages("Undetermined specific message")
                                .addPassGeneralMessages("Undetermined general message")
                                .build()),
                ImmutableEvaluation.Builder::addUndeterminedSpecificMessages,
                ImmutableEvaluation.Builder::addUndeterminedGeneralMessages,
                EvaluationResult.UNDETERMINED);
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation);
        assertThat(evaluation.undeterminedSpecificMessages()).containsOnly(
                "Undetermined specific message. Tumor stage has been implied to be I or II");
        assertThat(evaluation.undeterminedGeneralMessages()).containsOnly("Undetermined general message");
    }
}