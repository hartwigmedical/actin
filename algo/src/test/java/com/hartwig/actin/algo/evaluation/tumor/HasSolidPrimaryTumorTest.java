package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSolidPrimaryTumorTest {

    @Test
    public void canEvaluate() {
        HasSolidPrimaryTumor function = new HasSolidPrimaryTumor(createTestDoidModel());

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID)));

        String firstWarnDoid = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.iterator().next();
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid)));

        String firstNonSolidDoid = HasSolidPrimaryTumor.NON_SOLID_CANCER_DOIDS.iterator().next();
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid, firstNonSolidDoid)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")));
    }

    @NotNull
    private static DoidModel createTestDoidModel() {
        Map<String, String> childParentMap = Maps.newHashMap();
        for (String nonSolidDoid : HasSolidPrimaryTumor.NON_SOLID_CANCER_DOIDS) {
            childParentMap.put(nonSolidDoid, DoidConstants.CANCER_DOID);
        }

        for (String warnDoid : HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS) {
            childParentMap.put(warnDoid, DoidConstants.CANCER_DOID);
        }

        return TestDoidModelFactory.createWithChildToParentMap(childParentMap);
    }
}