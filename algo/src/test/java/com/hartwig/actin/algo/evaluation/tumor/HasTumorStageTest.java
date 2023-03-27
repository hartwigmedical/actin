package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Before;
import org.junit.Test;

public class HasTumorStageTest {

    private TumorStageDerivationFunction derivationFunction;
    private HasTumorStage victim;

    @Before
    public void setUp() {
        derivationFunction = mock(TumorStageDerivationFunction.class);
        victim = new HasTumorStage(derivationFunction, TumorStage.III);
    }

    @Test
    public void canEvaluate() {
        when(derivationFunction.apply(any())).thenReturn(Stream.empty());
        assertEvaluation(EvaluationResult.FAIL, victim.evaluate(TumorTestFactory.withTumorStage(null)));
        assertEvaluation(EvaluationResult.PASS, victim.evaluate(TumorTestFactory.withTumorStage(TumorStage.III)));
        assertEvaluation(EvaluationResult.PASS, victim.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIB)));
        assertEvaluation(EvaluationResult.FAIL, victim.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)));
    }

    @Test
    public void singleDerivedTumorShouldFollowNonDerivedEvaluation() {
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.III);
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.IIIB);
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.IV);
    }

    @Test
    public void multipleDerivedTumorStagesWhereOnePassesShouldBeEvaluatedUndetermined() {
        assertDerivedEvaluation(EvaluationResult.UNDETERMINED, TumorStage.III, TumorStage.IIIB);
    }

    @Test
    public void multipleDerivedTumorStagesWhereAllFailShouldBeEvaluatedFail() {
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.I, TumorStage.II);
    }

    private void assertDerivedEvaluation(EvaluationResult expectedResult, TumorStage... derivedStages) {
        PatientRecord patientRecord = TumorTestFactory.withTumorStage(null);
        TumorDetails tumorDetails = patientRecord.clinical().tumor();
        when(derivationFunction.apply(tumorDetails)).thenReturn(Stream.of(derivedStages));
        assertEvaluation(expectedResult, victim.evaluate(patientRecord));
    }
}