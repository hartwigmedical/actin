package com.hartwig.actin.algo.evaluation.tumor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationAssert;
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
    public void shouldReturnOriginalFunctionWhenTumorDetailsNotNull() {
        Evaluation originalEvaluation = evaluationWith(EvaluationResult.PASS);
        when(evaluationFunction.evaluate(TestDataFactory.createProperTestPatientRecord())).thenReturn(originalEvaluation);
        assertThat(victim.evaluate(TestDataFactory.createProperTestPatientRecord())).isEqualTo(originalEvaluation);
    }

    @Test
    public void shouldReturnOriginalFunctionWhenNoDerivedStagesPossible() {
        Evaluation originalEvaluation = evaluationWith(EvaluationResult.UNDETERMINED);
        when(evaluationFunction.evaluate(TestDataFactory.createMinimalTestPatientRecord())).thenReturn(originalEvaluation);
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord()
                .clinical()
                .tumor())).thenReturn(Stream.empty());
        assertThat(victim.evaluate(TestDataFactory.createMinimalTestPatientRecord())).isEqualTo(originalEvaluation);
    }

    @Test
    public void shouldFollowEvaluationWhenAnySingleInferredStage() {
        for (EvaluationResult evaluationResult : EvaluationResult.values()) {
            assertSingleStageWithResult(evaluationResult);
        }
    }

    @Test
    public void shouldEvaluatePassWhenMultipleDerivedStagesAllEvaluatePass() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Stream.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(evaluationWith(EvaluationResult.PASS));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(evaluationWith(EvaluationResult.PASS));

        Evaluation evaluation = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation);
    }

    @Test
    public void shouldEvaluatePassWhenMultipleDerivedAndAtLeastOnePasses() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Stream.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(evaluationWith(EvaluationResult.PASS));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(evaluationWith(EvaluationResult.FAIL));

        Evaluation evaluation = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation);
    }

    @Test
    public void shouldEvaluateFailWhenMultipleDerivedAndNoPassOrWarn() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Stream.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(evaluationWith(EvaluationResult.UNDETERMINED));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(evaluationWith(EvaluationResult.UNDETERMINED));

        Evaluation evaluation = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation);
    }

    @Test
    public void shouldEvaluateWarnWhenMultipleDerivedAndAtLeastOneWarnAndNoPass() {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Stream.of(
                TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(evaluationWith(EvaluationResult.WARN));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(evaluationWith(EvaluationResult.UNDETERMINED));

        Evaluation evaluation = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, evaluation);
    }

    private static Evaluation evaluationWith(EvaluationResult result) {
        return EvaluationFactory.unrecoverable().result(result).build();
    }

    private static PatientRecord withStage(TumorStage newStage) {
        return ImmutablePatientRecord.copyOf(TestDataFactory.createMinimalTestPatientRecord())
                .withClinical(ImmutableClinicalRecord.copyOf(TestDataFactory.createMinimalTestPatientRecord().clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())
                                .withStage(newStage)));
    }

    private void assertSingleStageWithResult(EvaluationResult expectedResult) {
        when(tumorStageDerivationFunction.apply(TestDataFactory.createMinimalTestPatientRecord().clinical().tumor())).thenReturn(Stream.of(
                TumorStage.I));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(evaluationWith(expectedResult));
        Evaluation evaluation = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        EvaluationAssert.assertEvaluation(expectedResult, evaluation);
    }
}