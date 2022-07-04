package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DoidEvaluationFunctionsTest {

    private static final String MATCH_DOID = "1";
    private static final Set<String> FAIL_DOIDS = Sets.newHashSet("2", "3");
    private static final Set<String> WARN_DOIDS = Sets.newHashSet("4", "5");

    @Test
    public void canEvaluate() {
        assertEquals(EvaluationResult.PASS, evaluate(MATCH_DOID));

        String firstWarnDoid = WARN_DOIDS.iterator().next();
        assertEquals(EvaluationResult.WARN, evaluate(MATCH_DOID, firstWarnDoid));

        String firstFailDoid = FAIL_DOIDS.iterator().next();
        assertEquals(EvaluationResult.FAIL, evaluate(MATCH_DOID, firstWarnDoid, firstFailDoid));

        assertEquals(EvaluationResult.FAIL, evaluate("arbitrary doid"));
    }

    @NotNull
    private static EvaluationResult evaluate(@NotNull String... patientDoids) {
        return DoidEvaluationFunctions.evaluate(createTestDoidModel(), Sets.newHashSet(patientDoids), MATCH_DOID, FAIL_DOIDS, WARN_DOIDS);
    }

    @NotNull
    private static DoidModel createTestDoidModel() {
        Map<String, String> childParentMap = Maps.newHashMap();
        for (String failDoid : FAIL_DOIDS) {
            childParentMap.put(failDoid, MATCH_DOID);
        }

        for (String warnDoid : WARN_DOIDS) {
            childParentMap.put(warnDoid, MATCH_DOID);
        }

        return TestDoidModelFactory.createWithChildParentMap(childParentMap);
    }
}