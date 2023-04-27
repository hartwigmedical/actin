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
    public void canDetermineIfTumorHasConfiguredDoids() {
        assertFalse(DoidEvaluationFunctions.hasConfiguredDoids(null));
        assertFalse(DoidEvaluationFunctions.hasConfiguredDoids(Sets.newHashSet()));
        assertTrue(DoidEvaluationFunctions.hasConfiguredDoids(Sets.newHashSet("yes!")));
    }

    @Test
    public void canDetermineIfTumorIsOfDoidType() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child");

        assertFalse(DoidEvaluationFunctions.isOfDoidType(doidModel, null, "child"));
        assertFalse(DoidEvaluationFunctions.isOfDoidType(doidModel, Sets.newHashSet("parent"), "child"));
        assertTrue(DoidEvaluationFunctions.isOfDoidType(doidModel, Sets.newHashSet("child"), "child"));
        assertTrue(DoidEvaluationFunctions.isOfDoidType(doidModel, Sets.newHashSet("child"), "parent"));
        assertTrue(DoidEvaluationFunctions.isOfDoidType(doidModel, Sets.newHashSet("child", "other"), "parent"));
    }

    @Test
    public void canDetermineIfTumorIsOfLeastOneDoidType() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child");

        assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, null, Sets.newHashSet("child")));
        assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, Sets.newHashSet("parent"), Sets.newHashSet("child")));
        assertTrue(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel,
                Sets.newHashSet("child"),
                Sets.newHashSet("child", "other")));
        assertTrue(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, Sets.newHashSet("child"), Sets.newHashSet("parent")));
        assertTrue(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel,
                Sets.newHashSet("child", "other"),
                Sets.newHashSet("and another", "parent")));
    }

    @Test
    public void canDetermineIfTumorIsOfAtLeastOneDoidTerm() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("match doid", "match doid term");

        Set<String> validDoidTerms = Sets.newHashSet("match doid term");

        assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, null, validDoidTerms));
        assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, Sets.newHashSet(), validDoidTerms));
        assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, Sets.newHashSet("wrong"), validDoidTerms));
        assertTrue(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, Sets.newHashSet("match doid") , validDoidTerms));
    }

    @Test
    public void canDetermineIfTumorHasExactDoid() {
        assertFalse(DoidEvaluationFunctions.isOfExactDoid(null, "1"));
        assertFalse(DoidEvaluationFunctions.isOfExactDoid(Sets.newHashSet("1", "2"), "1"));
        assertFalse(DoidEvaluationFunctions.isOfExactDoid(Sets.newHashSet("1", "2"), "2"));
        assertFalse(DoidEvaluationFunctions.isOfExactDoid(Sets.newHashSet("2"), "1"));
        assertTrue(DoidEvaluationFunctions.isOfExactDoid(Sets.newHashSet("1"), "1"));
    }

    @Test
    public void canDetermineIfTumorIsOfDoidCombinationType() {
        assertFalse(DoidEvaluationFunctions.isOfDoidCombinationType(null, Sets.newHashSet("1", "2")));
        assertFalse(DoidEvaluationFunctions.isOfDoidCombinationType(Sets.newHashSet("1"), Sets.newHashSet("1", "2")));
        assertTrue(DoidEvaluationFunctions.isOfDoidCombinationType(Sets.newHashSet("1", "2"), Sets.newHashSet("2")));
        assertTrue(DoidEvaluationFunctions.isOfDoidCombinationType(Sets.newHashSet("1", "2"), Sets.newHashSet("2", "1")));
    }

    @Test
    public void canDetermineIfTumorIsOfExclusiveDoidType() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child");

        assertFalse(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, null, "child"));
        assertFalse(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, Sets.newHashSet("parent"), "child"));
        assertTrue(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, Sets.newHashSet("child"), "child"));
        assertTrue(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, Sets.newHashSet("child"), "parent"));
        assertFalse(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, Sets.newHashSet("child", "other"), "parent"));
    }

    @Test
    public void canDetermineIfDoidIsExclusiveType() {
        assertEquals(EvaluationResult.PASS, hasExclusiveTumorTypeOfDoid(MATCH_DOID));

        String firstWarnDoid = WARN_DOIDS.iterator().next();
        assertEquals(EvaluationResult.WARN, hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid));

        String firstFailDoid = FAIL_DOIDS.iterator().next();
        assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid, firstFailDoid));

        assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid("arbitrary doid"));
        assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid(MATCH_DOID, "arbitrary doid"));
    }

    @NotNull
    private static EvaluationResult hasExclusiveTumorTypeOfDoid(@NotNull String... tumorDoids) {
        return DoidEvaluationFunctions.evaluateForExclusiveMatchWithFailAndWarns(MATCHING_TEST_MODEL,
                Sets.newHashSet(tumorDoids),
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

        return TestDoidModelFactory.createWithChildToParentMap(childParentMap);
    }

    @Test
    public void canEvaluateIfPatientHasSpecificDoidCombination() {
        Set<String> tumorDoids = Sets.newHashSet("1", "2", "3");

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

        assertFalse(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(null, combinationSet1));
        assertFalse(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet1));
        assertTrue(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet2));
        assertTrue(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet3));
        assertTrue(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet4));
    }
}