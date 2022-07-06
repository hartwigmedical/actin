package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSolidPrimaryTumorIncludingLymphomaTest {

    @Test
    public void canEvaluate() {
        HasSolidPrimaryTumorIncludingLymphoma function = new HasSolidPrimaryTumorIncludingLymphoma(createTestDoidModel());

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withDoids(HasSolidPrimaryTumorIncludingLymphoma.CANCER_DOID)));

        String firstWarnDoid = HasSolidPrimaryTumorIncludingLymphoma.WARN_SOLID_CANCER_DOIDS.iterator().next();
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(TumorTestFactory.withDoids(HasSolidPrimaryTumorIncludingLymphoma.CANCER_DOID, firstWarnDoid)));

        String firstNonSolidDoid = HasSolidPrimaryTumorIncludingLymphoma.NON_SOLID_CANCER_DOIDS.iterator().next();
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withDoids(HasSolidPrimaryTumorIncludingLymphoma.CANCER_DOID,
                        firstWarnDoid,
                        firstNonSolidDoid)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")));
    }

    @NotNull
    private static DoidModel createTestDoidModel() {
        Map<String, String> childParentMap = Maps.newHashMap();
        for (String nonSolidDoid : HasSolidPrimaryTumorIncludingLymphoma.NON_SOLID_CANCER_DOIDS) {
            childParentMap.put(nonSolidDoid, HasSolidPrimaryTumorIncludingLymphoma.CANCER_DOID);
        }

        for (String warnDoid : HasSolidPrimaryTumorIncludingLymphoma.WARN_SOLID_CANCER_DOIDS) {
            childParentMap.put(warnDoid, HasSolidPrimaryTumorIncludingLymphoma.CANCER_DOID);
        }

        return TestDoidModelFactory.createWithChildParentMap(childParentMap);
    }

}