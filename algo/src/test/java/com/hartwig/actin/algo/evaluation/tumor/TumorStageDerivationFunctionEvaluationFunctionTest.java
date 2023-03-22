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

public class TumorStageDerivationFunctionEvaluationFunctionTest {

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
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(TumorStage.I));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                ImmutableEvaluation.builder().result(EvaluationResult.PASS).recoverable(false).build());
        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.PASS);
        assertThat(evaluate.passGeneralMessages()).contains("Implied tumor stage of type [I]");
    }

    @Test
    public void multipleInferredStagesEvaluatesPassWhenAllPass() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationBuilderWith(EvaluationResult.PASS).addPassGeneralMessages("pass general 1")
                        .addPassSpecificMessages("pass specific 1")
                        .build());
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationBuilderWith(EvaluationResult.PASS).addPassGeneralMessages("pass general 2")
                        .addPassSpecificMessages("pass specific 2")
                        .build());

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.PASS);
        assertThat(evaluate.passGeneralMessages()).contains("pass general 1", "pass general 2");
        assertThat(evaluate.passSpecificMessages()).contains("pass specific 1", "pass specific 2");
    }

    @Test
    public void multipleInferredStagesEvaluatesUnderminedIfOnePasses() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Set.of(TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.I))).thenReturn(
                evaluationBuilderWith(EvaluationResult.PASS).addPassGeneralMessages("pass general 1")
                        .addPassSpecificMessages("pass specific 1")
                        .build());
        when(evaluationFunction.evaluate(withStage(TestDataFactory.createMinimalTestPatientRecord(), TumorStage.II))).thenReturn(
                evaluationBuilderWith(EvaluationResult.FAIL).addFailGeneralMessages("fail general 2")
                        .addFailSpecificMessages("fail specific 2")
                        .build());

        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat(evaluate.result()).isEqualTo(EvaluationResult.UNDETERMINED);
        assertThat(evaluate.undeterminedGeneralMessages()).contains("pass general 1", "fail general 2");
        assertThat(evaluate.undeterminedSpecificMessages()).contains("pass specific 1", "fail specific 2");
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