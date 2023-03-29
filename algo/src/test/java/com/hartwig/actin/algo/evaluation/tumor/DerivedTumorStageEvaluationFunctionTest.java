package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.datamodel.EvaluationTestFactory.withResult;
import static com.hartwig.actin.algo.evaluation.EvaluationAssert.*;

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
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Before;
import org.junit.Test;

public class DerivedTumorStageEvaluationFunctionTest {

    private static final PatientRecord PROPER_TEST_PATIENT_RECORD = TestDataFactory.createProperTestPatientRecord();
    private static final PatientRecord MINIMAL_TEST_PATIENT_RECORD = TestDataFactory.createMinimalTestPatientRecord();
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
        Evaluation originalEvaluation = withResult(EvaluationResult.PASS);
        when(evaluationFunction.evaluate(PROPER_TEST_PATIENT_RECORD)).thenReturn(originalEvaluation);
        assertThat(victim.evaluate(PROPER_TEST_PATIENT_RECORD)).isEqualTo(originalEvaluation);
    }

    @Test
    public void shouldReturnOriginalFunctionWhenNoDerivedStagesPossible() {
        Evaluation originalEvaluation = withResult(EvaluationResult.UNDETERMINED);
        when(evaluationFunction.evaluate(MINIMAL_TEST_PATIENT_RECORD)).thenReturn(originalEvaluation);
        when(tumorStageDerivationFunction.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(Stream.empty());
        assertThat(victim.evaluate(MINIMAL_TEST_PATIENT_RECORD)).isEqualTo(originalEvaluation);
    }

    @Test
    public void shouldFollowEvaluationWhenAnySingleInferredStage() {
        for (EvaluationResult evaluationResult : EvaluationResult.values()) {
            assertSingleStageWithResult(evaluationResult);
        }
    }

    @Test
    public void shouldEvaluatePassWhenMultipleDerivedStagesAllEvaluatePass() {
        when(tumorStageDerivationFunction.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(Stream.of(TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(withResult(EvaluationResult.PASS));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(withResult(EvaluationResult.PASS));

        Evaluation evaluation = victim.evaluate(MINIMAL_TEST_PATIENT_RECORD);
        assertEvaluation(EvaluationResult.PASS, evaluation);
    }

    @Test
    public void shouldEvaluatePassWhenMultipleDerivedAndAtLeastOnePasses() {
        when(tumorStageDerivationFunction.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(Stream.of(TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(withResult(EvaluationResult.PASS));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(withResult(EvaluationResult.FAIL));

        Evaluation evaluation = victim.evaluate(MINIMAL_TEST_PATIENT_RECORD);
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation);
    }

    @Test
    public void shouldEvaluateFailWhenMultipleDerivedAndNoPassOrWarn() {
        when(tumorStageDerivationFunction.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(Stream.of(TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(withResult(EvaluationResult.UNDETERMINED));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(withResult(EvaluationResult.UNDETERMINED));

        Evaluation evaluation = victim.evaluate(MINIMAL_TEST_PATIENT_RECORD);
        assertEvaluation(EvaluationResult.FAIL, evaluation);
    }

    @Test
    public void shouldEvaluateWarnWhenMultipleDerivedAndAtLeastOneWarnAndNoPass() {
        when(tumorStageDerivationFunction.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(Stream.of(TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(withResult(EvaluationResult.WARN));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(withResult(EvaluationResult.UNDETERMINED));

        Evaluation evaluation = victim.evaluate(MINIMAL_TEST_PATIENT_RECORD);
        assertEvaluation(EvaluationResult.WARN, evaluation);
    }

    @Test
    public void shouldEvaluateNotEvaluatedWhenMultipleDerivedAndAtAllAreNotEvaluated() {
        when(tumorStageDerivationFunction.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(Stream.of(TumorStage.I,
                TumorStage.II));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(withResult(EvaluationResult.NOT_EVALUATED));
        when(evaluationFunction.evaluate(withStage(TumorStage.II))).thenReturn(withResult(EvaluationResult.NOT_EVALUATED));

        Evaluation evaluation = victim.evaluate(MINIMAL_TEST_PATIENT_RECORD);
        assertEvaluation(EvaluationResult.NOT_EVALUATED, evaluation);
    }

    private static PatientRecord withStage(TumorStage newStage) {
        return ImmutablePatientRecord.copyOf(MINIMAL_TEST_PATIENT_RECORD)
                .withClinical(ImmutableClinicalRecord.copyOf(MINIMAL_TEST_PATIENT_RECORD.clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()).withStage(newStage)));
    }

    private void assertSingleStageWithResult(EvaluationResult expectedResult) {
        when(tumorStageDerivationFunction.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(Stream.of(TumorStage.I));
        when(evaluationFunction.evaluate(withStage(TumorStage.I))).thenReturn(withResult(expectedResult));
        Evaluation evaluation = victim.evaluate(MINIMAL_TEST_PATIENT_RECORD);
        assertEvaluation(expectedResult, evaluation);
    }
}