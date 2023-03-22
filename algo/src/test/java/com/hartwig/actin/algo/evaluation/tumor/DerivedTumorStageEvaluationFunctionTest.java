package com.hartwig.actin.algo.evaluation.tumor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Before;
import org.junit.Test;

public class DerivedTumorStageEvaluationFunctionTest {

    private static final String GENERAL_1 = "general 1";
    private static final String SPECIFIC_1 = "specific 1";
    private static final String GENERAL_2 = "general 2";
    private static final String SPECIFIC_2 = "specific 2";
    private DerivedTumorStageEvaluationFunction victim;
    private TumorStageDerivationFunction tumorStageDerivationFunction;
    private EvaluationFunction evaluationFunction;

    @Before
    public void setUp() {
        tumorStageDerivationFunction = mock(TumorStageDerivationFunction.class);
        evaluationFunction = mock(EvaluationFunction.class);
        victim = new DerivedTumorStageEvaluationFunction(tumorStageDerivationFunction, evaluationFunction);
    }

    @Test
    public void singleInferredStageEvaluatesAndAddsImpliedMessage() {
        for (EvaluationResult result : EvaluationResult.values()) {
            when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                    TumorStage.I));
            when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                    ImmutableEvaluation.builder().result(result).addPassGeneralMessages("message 1").recoverable(false).build());
            Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
            assertThat(evaluate.result()).isEqualTo(result);
            assertThat(evaluate.passGeneralMessages()).contains("message 1 [Implied by derived tumor stage I]");
        }
    }

    @Test
    public void multipleInferredStagesEvaluatesPassWhenAllPass() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationBuilderWith(EvaluationResult.PASS).addPassGeneralMessages(GENERAL_1).addPassSpecificMessages(SPECIFIC_1).build());
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationBuilderWith(EvaluationResult.PASS).addPassGeneralMessages(GENERAL_2).addPassSpecificMessages(SPECIFIC_2).build());

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.PASS);
        assertThat(evaluate.passGeneralMessages()).containsExactlyInAnyOrder("general 1 [Implied by derived tumor stage I]",
                "general 2 [Implied by derived tumor stage II]");
        assertThat(evaluate.passSpecificMessages()).containsExactlyInAnyOrder("specific 1 [Implied by derived tumor stage I]",
                "specific 2 [Implied by derived tumor stage II]");
    }

    @Test
    public void multipleInferredStagesEvaluatesUnderminedIfAtLeastOnePasses() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationBuilderWith(EvaluationResult.PASS).addPassGeneralMessages(GENERAL_1).addPassSpecificMessages(SPECIFIC_1).build());
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationBuilderWith(EvaluationResult.FAIL).addFailGeneralMessages(GENERAL_2).addFailSpecificMessages(SPECIFIC_2).build());

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.UNDETERMINED);
        assertThat(evaluate.undeterminedGeneralMessages()).containsExactlyInAnyOrder("general 1 [Implied by derived tumor stage I]",
                "general 2 [Implied by derived tumor stage II]");
        assertThat(evaluate.undeterminedSpecificMessages()).containsExactlyInAnyOrder("specific 1 [Implied by derived tumor stage I]",
                "specific 2 [Implied by derived tumor stage II]");
    }

    @Test
    public void multipleInferredStagesEvaluatesFailedIfNoPassOrWarn() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationBuilderWith(EvaluationResult.UNDETERMINED).addPassGeneralMessages(GENERAL_1)
                        .addPassSpecificMessages(SPECIFIC_1)
                        .build());
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationBuilderWith(EvaluationResult.UNDETERMINED).addFailGeneralMessages(GENERAL_2)
                        .addFailSpecificMessages(SPECIFIC_2)
                        .build());

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.FAIL);
        assertThat(evaluate.failGeneralMessages()).containsExactlyInAnyOrder("general 1 [Implied by derived tumor stage I]",
                "general 2 [Implied by derived tumor stage II]");
        assertThat(evaluate.failSpecificMessages()).containsExactlyInAnyOrder("specific 1 [Implied by derived tumor stage I]",
                "specific 2 [Implied by derived tumor stage II]");
    }

    @Test
    public void multipleInferredStagesEvaluatesWarnIfAtLeastOneWarnsAndNoPasses() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationBuilderWith(EvaluationResult.WARN).addPassGeneralMessages(GENERAL_1).addPassSpecificMessages(SPECIFIC_1).build());
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationBuilderWith(EvaluationResult.UNDETERMINED).addUndeterminedGeneralMessages(GENERAL_2)
                        .addUndeterminedSpecificMessages(SPECIFIC_2)
                        .build());

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.WARN);
        assertThat(evaluate.warnGeneralMessages()).containsExactlyInAnyOrder("general 1 [Implied by derived tumor stage I]",
                "general 2 [Implied by derived tumor stage II]");
        assertThat(evaluate.warnSpecificMessages()).containsExactlyInAnyOrder("specific 1 [Implied by derived tumor stage I]",
                "specific 2 [Implied by derived tumor stage II]");
    }

    private static ImmutableEvaluation.Builder evaluationBuilderWith(final EvaluationResult result) {
        return ImmutableEvaluation.builder().result(result).recoverable(false);
    }

    private static PatientRecord withStage(final PatientRecord originalRecord, final TumorStage newStage) {
        return ImmutablePatientRecord.copyOf(originalRecord)
                .withClinical(ImmutableClinicalRecord.copyOf(originalRecord.clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(originalRecord.clinical().tumor()).withStage(newStage)));
    }

}