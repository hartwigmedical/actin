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
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Before;
import org.junit.Test;

public class DerivedTumorStageEvaluationFunctionTest {

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
    public void singleInferredStageEvaluatesFollowEvaluation() {
        assertSingleStageWithResult(EvaluationResult.PASS);
        assertSingleStageWithResult(EvaluationResult.WARN);
        assertSingleStageWithResult(EvaluationResult.UNDETERMINED);
        assertSingleStageWithResult(EvaluationResult.FAIL);
    }

    @Test
    public void multipleInferredStagesEvaluatesPassWhenAllPass() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationWith(EvaluationResult.PASS));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationWith(EvaluationResult.PASS));

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.PASS);
    }

    @Test
    public void multipleInferredStagesEvaluatesUnderminedIfAtLeastOnePasses() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationWith(EvaluationResult.PASS));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationWith(EvaluationResult.FAIL));

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.UNDETERMINED);
    }

    @Test
    public void multipleInferredStagesEvaluatesFailedIfNoPassOrWarn() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationWith(EvaluationResult.UNDETERMINED));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationWith(EvaluationResult.UNDETERMINED));

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.FAIL);
    }

    @Test
    public void multipleInferredStagesEvaluatesWarnIfAtLeastOneWarnsAndNoPasses() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationWith(EvaluationResult.WARN));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationWith(EvaluationResult.UNDETERMINED));

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.WARN);
    }

    private static Evaluation evaluationWith(final EvaluationResult result) {
        return EvaluationFactory.unrecoverable().result(result).displayName("test").build();
    }

    private static PatientRecord withStage(final PatientRecord originalRecord, final TumorStage newStage) {
        return ImmutablePatientRecord.copyOf(originalRecord)
                .withClinical(ImmutableClinicalRecord.copyOf(originalRecord.clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(originalRecord.clinical().tumor()).withStage(newStage)));
    }

    private void assertSingleStageWithResult(EvaluationResult pass) {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(
                TumorStage.I));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                EvaluationFactory.recoverable().result(pass).build());
        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(pass);
    }

}