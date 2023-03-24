package com.hartwig.actin.algo.evaluation.tumor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Test;

public class DerivedTumorStageEvaluationFactoryTest {

    private static final String SPECIFIC_MESSAGE = "Patient has unresectable cancer";
    private static final String GENERAL_MESSAGE = "Unresectable cancer";

    @Test
    public void createEvaluationWithDerivedDataForPass() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.pass(Map.of(TumorStage.IV,
                evaluationWithResult(EvaluationResult.PASS),
                TumorStage.III,
                evaluationWithResult(EvaluationResult.PASS)));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.PASS);
        assertThat(evaluation.passSpecificMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization, tumor stage should be [III or IV]. All derived locations indicate [Patient has unresectable cancer]");
        assertThat(evaluation.passGeneralMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization, tumor stage should be [III or IV]. All derived locations indicate [Unresectable cancer]");
    }

    @Test
    public void createEvaluationWithDerivedDataForUndetermined() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.undetermined(Map.of(TumorStage.IV,
                evaluationWithResult(EvaluationResult.PASS),
                TumorStage.III,
                evaluationWithResult(EvaluationResult.FAIL)));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.UNDETERMINED);
        assertThat(evaluation.undeterminedSpecificMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization tumor stage should be [III or IV]. Only tumor stage [IV] passed with message [Patient has unresectable cancer], but the others did not, hence the result the is undetermined");
        assertThat(evaluation.undeterminedGeneralMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization tumor stage should be [III or IV]. Only tumor stage [IV] passed with message [Unresectable cancer], but the others did not, hence the result the is undetermined");
    }

    @Test
    public void createEvaluationWithDerivedDataForWarn() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.warn(Map.of(TumorStage.IV,
                evaluationWithResult(EvaluationResult.PASS),
                TumorStage.III,
                evaluationWithResult(EvaluationResult.FAIL)));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.WARN);
        assertThat(evaluation.warnSpecificMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization tumor stage should be [III or IV]. There was at least one warning with message [Patient has unresectable cancer], hence the result the is warning");
        assertThat(evaluation.warnGeneralMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization tumor stage should be [III or IV]. There was at least one warning with message [Unresectable cancer], hence the result the is warning");
    }

    @Test
    public void createEvaluationWithDerivedDataForFail() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.fail(Map.of(TumorStage.IV,
                evaluationWithResult(EvaluationResult.UNDETERMINED),
                TumorStage.III,
                evaluationWithResult(EvaluationResult.FAIL)));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.FAIL);
        assertThat(evaluation.failSpecificMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization tumor stage should be [III or IV]. None of these possible staged passed on warned, hence the result is a failure");
        assertThat(evaluation.failGeneralMessages()).containsOnly(
                "Tumor stage details are missing. Based on lesion localization tumor stage should be [III or IV]. None of these possible staged passed on warned, hence the result is a failure");
    }

    private static ImmutableEvaluation evaluationWithResult(EvaluationResult pass) {
        return EvaluationFactory.unrecoverable()
                .result(pass)
                .addPassSpecificMessages(SPECIFIC_MESSAGE)
                .addPassGeneralMessages(GENERAL_MESSAGE)
                .addUndeterminedSpecificMessages(SPECIFIC_MESSAGE)
                .addUndeterminedGeneralMessages(GENERAL_MESSAGE)
                .addWarnSpecificMessages(SPECIFIC_MESSAGE)
                .addWarnGeneralMessages(GENERAL_MESSAGE)
                .addFailSpecificMessages(SPECIFIC_MESSAGE)
                .addFailGeneralMessages(GENERAL_MESSAGE)
                .build();
    }
}