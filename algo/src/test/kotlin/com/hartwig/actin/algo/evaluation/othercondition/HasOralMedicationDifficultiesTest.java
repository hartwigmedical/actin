package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;

import org.junit.Test;

public class HasOralMedicationDifficultiesTest {

    @Test
    public void canEvaluate() {
        HasOralMedicationDifficulties function = new HasOralMedicationDifficulties();

        // Test no complications
        List<Complication> complications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)));

        // Add a random complication
        complications.add(ImmutableComplication.builder().name("not a problem").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)));

        // Add a real shallow difficulty one
        String oralMedicationComplication = HasOralMedicationDifficulties.COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES.iterator().next();
        complications.add(ImmutableComplication.builder().name(oralMedicationComplication).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(complications)));
    }
}