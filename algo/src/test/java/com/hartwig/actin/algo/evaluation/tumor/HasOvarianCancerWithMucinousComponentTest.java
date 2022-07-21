package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasOvarianCancerWithMucinousComponentTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        HasOvarianCancerWithMucinousComponent function = new HasOvarianCancerWithMucinousComponent(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        PatientRecord matchSingle =
                TumorTestFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOIDS.iterator().next());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchSingle));

        PatientRecord matchCombination =
                TumorTestFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOID_SETS.iterator().next());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchCombination));

        PatientRecord somethingElse = TumorTestFactory.withDoids("something else");
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(somethingElse));
    }
}