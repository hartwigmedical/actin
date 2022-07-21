package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DoidEvaluationFunctionsTest {

    private static final String MATCH_DOID = "1";
    private static final Set<String> FAIL_DOIDS = Sets.newHashSet("2", "3");
    private static final Set<String> WARN_DOIDS = Sets.newHashSet("4", "5");

    private static final DoidModel MATCHING_TEST_MODEL = createTestDoidModelForMatching();

    @Test
    public void canMatchToExclusiveTumorDoid() {
        assertEquals(EvaluationResult.PASS, hasExclusiveTumorTypeOfDoid(MATCH_DOID));

        String firstWarnDoid = WARN_DOIDS.iterator().next();
        assertEquals(EvaluationResult.WARN, hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid));

        String firstFailDoid = FAIL_DOIDS.iterator().next();
        assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid, firstFailDoid));

        assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid("arbitrary doid"));
        assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid(MATCH_DOID, "arbitrary doid"));
    }

    @NotNull
    private static EvaluationResult hasExclusiveTumorTypeOfDoid(@NotNull String... patientDoids) {
        return DoidEvaluationFunctions.hasExclusiveTumorTypeOfDoid(MATCHING_TEST_MODEL,
                Sets.newHashSet(patientDoids),
                MATCH_DOID,
                FAIL_DOIDS,
                WARN_DOIDS);
    }

    @NotNull
    private static DoidModel createTestDoidModelForMatching() {
        Map<String, String> childParentMap = Maps.newHashMap();
        for (String failDoid : FAIL_DOIDS) {
            childParentMap.put(failDoid, MATCH_DOID);
        }

        for (String warnDoid : WARN_DOIDS) {
            childParentMap.put(warnDoid, MATCH_DOID);
        }

        return TestDoidModelFactory.createWithChildParentMap(childParentMap);
    }

    @Test
    public void canDetermineIfTumorIsOfType() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("other doid", "match doid term");

        Set<String> validDoids = Sets.newHashSet("match doid");
        Set<String> validDoidTerms = Sets.newHashSet("match doid term");
        Set<String> validPrimaryTumorExtraDetails = Sets.newHashSet("match details");

        assertFalse(DoidEvaluationFunctions.hasTumorOfCertainType(doidModel,
                TumorTestFactory.builder().build(),
                validDoids,
                validDoidTerms,
                validPrimaryTumorExtraDetails));

        assertTrue(DoidEvaluationFunctions.hasTumorOfCertainType(doidModel,
                TumorTestFactory.builder().addDoids("match doid").build(),
                validDoids,
                validDoidTerms,
                validPrimaryTumorExtraDetails));

        assertTrue(DoidEvaluationFunctions.hasTumorOfCertainType(doidModel,
                TumorTestFactory.builder().addDoids("other doid").build(),
                validDoids,
                validDoidTerms,
                validPrimaryTumorExtraDetails));

        assertFalse(DoidEvaluationFunctions.hasTumorOfCertainType(doidModel,
                TumorTestFactory.builder().primaryTumorExtraDetails("wrong").build(),
                validDoids,
                validDoidTerms,
                validPrimaryTumorExtraDetails));

        assertTrue(DoidEvaluationFunctions.hasTumorOfCertainType(doidModel,
                TumorTestFactory.builder().primaryTumorExtraDetails("tumor match details").build(),
                validDoids,
                validDoidTerms,
                validPrimaryTumorExtraDetails));
    }

    @Test
    public void canEvaluateIfPatientHasSpecificDoidCombination() {
        Set<String> patientDoids = Sets.newHashSet("1", "2", "3");

        Set<String> set1 = Sets.newHashSet("1", "4");
        Set<String> set2 = Sets.newHashSet("2", "3");
        Set<String> set3 = Sets.newHashSet("1");

        Set<Set<String>> combinationSet1 = Sets.newHashSet();
        combinationSet1.add(set1);

        Set<Set<String>> combinationSet2 = Sets.newHashSet();
        combinationSet2.add(set2);

        Set<Set<String>> combinationSet3 = Sets.newHashSet();
        combinationSet3.add(set3);

        Set<Set<String>> combinationSet4 = Sets.newHashSet();
        combinationSet4.add(set1);
        combinationSet4.add(set2);

        assertFalse(DoidEvaluationFunctions.hasSpecificCombinationOfDoids(patientDoids, combinationSet1));
        assertTrue(DoidEvaluationFunctions.hasSpecificCombinationOfDoids(patientDoids, combinationSet2));
        assertTrue(DoidEvaluationFunctions.hasSpecificCombinationOfDoids(patientDoids, combinationSet3));
        assertTrue(DoidEvaluationFunctions.hasSpecificCombinationOfDoids(patientDoids, combinationSet4));
    }
}