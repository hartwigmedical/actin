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

    public static final String HAS_UNRESECTABLE_CANCER = "has unresectable cancer";

    @Test
    public void followGivenStageUsesCorrectFunction() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.follow(Map.entry(TumorStage.IV,
                evaluationWithResult(EvaluationResult.PASS, HAS_UNRESECTABLE_CANCER).addPassSpecificMessages(
                        "Tumor stage IV is considered unresectable cancer").build()));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.PASS);
        assertThat(evaluation.passSpecificMessages()).containsOnly(
                "Tumor stage details are missing but based on lesion localization tumor stage should be IV. "
                        + "Tumor stage IV is considered unresectable cancer.");
        assertThat(evaluation.passGeneralMessages()).containsOnly("Derived tumor stage of IV has unresectable cancer");
    }

    @Test
    public void createEvaluationWithDerivedDataForPass() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.pass(Map.of(TumorStage.IV,
                evaluationWithResult(EvaluationResult.PASS, HAS_UNRESECTABLE_CANCER).addPassSpecificMessages(
                        "Tumor stage IV is considered unresectable cancer").build()));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.PASS);
        assertThat(evaluation.passSpecificMessages()).containsOnly(
                "Tumor stage details are missing but based on lesion localization tumor stage should be IV. "
                        + "Tumor stage IV is considered unresectable cancer.");
        assertThat(evaluation.passGeneralMessages()).containsOnly("Derived tumor stage of IV has unresectable cancer");
    }

    @Test
    public void createEvaluationWithDerivedDataForUndetermined() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.undetermined(Map.of(TumorStage.IV,
                evaluationWithResult(EvaluationResult.PASS, HAS_UNRESECTABLE_CANCER).addUndeterminedSpecificMessages(
                        "Stage IV is considered unresectable cancer.").build(),
                TumorStage.III,
                evaluationWithResult(EvaluationResult.FAIL, HAS_UNRESECTABLE_CANCER).addUndeterminedSpecificMessages(
                        "Stage III is unclear if patient has unresectable cancer").build()));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.UNDETERMINED);
        assertThat(evaluation.undeterminedSpecificMessages()).containsOnly(
                "Tumor stage details are missing but based on lesion localization tumor stage should be III or IV. "
                        + "Stage III is unclear if patient has unresectable cancer. Stage IV is considered unresectable cancer. "
                        + "It is undetermined whether the patient has unresectable cancer.");
        assertThat(evaluation.undeterminedGeneralMessages()).containsOnly(
                "From derived tumor stage of III or IV it is undetermined if has unresectable cancer");
    }

    @Test
    public void createEvaluationWithDerivedDataForWarn() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.warn(Map.of(TumorStage.II,
                evaluationWithResult(EvaluationResult.PASS, "is metastatic cancer").addWarnSpecificMessages(
                        "Stage II combined with brain, head or neck primary is potentially metastatic").build()));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.WARN);
        assertThat(evaluation.warnSpecificMessages()).containsOnly(
                "Tumor stage details are missing but based on lesion localization tumor stage should be II. Stage II combined with brain, head or neck primary is potentially metastatic.");
        assertThat(evaluation.warnGeneralMessages()).containsOnly("Derived tumor stage of II is metastatic cancer");
    }

    @Test
    public void createEvaluationWithDerivedDataForFail() {
        Evaluation evaluation = DerivedTumorStageEvaluationFactory.fail(Map.of(TumorStage.I,
                evaluationWithResult(EvaluationResult.FAIL, HAS_UNRESECTABLE_CANCER).addFailSpecificMessages(
                        "Stage I is not considered unresectable cancer").build(),
                TumorStage.II,
                evaluationWithResult(EvaluationResult.UNDETERMINED, HAS_UNRESECTABLE_CANCER).addFailSpecificMessages(
                        "Stage II is not considered unresectable cancer").build()));
        assertThat(evaluation.result()).isEqualTo(EvaluationResult.FAIL);
        assertThat(evaluation.failSpecificMessages()).containsOnly(
                "Tumor stage details are missing but based on lesion localization tumor stage should be I or II. Stage I is not considered unresectable cancer. Stage II is not considered unresectable cancer.");
        assertThat(evaluation.failGeneralMessages()).containsOnly("Derived tumor stage of I or II does not have unresectable cancer");
    }

    private static ImmutableEvaluation.Builder evaluationWithResult(EvaluationResult pass, String displayName) {
        return EvaluationFactory.unrecoverable().result(pass).displayName(displayName);
    }
}