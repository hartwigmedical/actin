package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Assert
import org.junit.Test

class DoidEvaluationFunctionsTest {

    @Test
    fun canDetermineIfTumorHasConfiguredDoids() {
        Assert.assertFalse(DoidEvaluationFunctions.hasConfiguredDoids(null))
        Assert.assertFalse(DoidEvaluationFunctions.hasConfiguredDoids(emptySet()))
        Assert.assertTrue(DoidEvaluationFunctions.hasConfiguredDoids(setOf("yes!")))
    }

    @Test
    fun canDetermineIfTumorIsOfDoidType() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        Assert.assertFalse(DoidEvaluationFunctions.isOfDoidType(doidModel, null, "child"))
        Assert.assertFalse(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("parent"), "child"))
        Assert.assertTrue(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("child"), "child"))
        Assert.assertTrue(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("child"), "parent"))
        Assert.assertTrue(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("child", "other"), "parent"))
    }

    @Test
    fun canDetermineIfTumorIsOfLeastOneDoidType() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        Assert.assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, null, setOf("child")))
        Assert.assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, setOf("parent"), setOf("child")))
        Assert.assertTrue(
            DoidEvaluationFunctions.isOfAtLeastOneDoidType(
                doidModel,
                setOf("child"),
                setOf("child", "other")
            )
        )
        Assert.assertTrue(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, setOf("child"), setOf("parent")))
        Assert.assertTrue(
            DoidEvaluationFunctions.isOfAtLeastOneDoidType(
                doidModel,
                setOf("child", "other"),
                setOf("and another", "parent")
            )
        )
    }

    @Test
    fun canDetermineIfTumorIsOfAtLeastOneDoidTerm() {
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("match doid", "match doid term")
        val validDoidTerms: Set<String> = setOf("match doid term")
        Assert.assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, null, validDoidTerms))
        Assert.assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, setOf(), validDoidTerms))
        Assert.assertFalse(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, setOf("wrong"), validDoidTerms))
        Assert.assertTrue(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, setOf("match doid"), validDoidTerms))
    }

    @Test
    fun canDetermineIfTumorHasExactDoid() {
        Assert.assertFalse(DoidEvaluationFunctions.isOfExactDoid(null, "1"))
        Assert.assertFalse(DoidEvaluationFunctions.isOfExactDoid(setOf("1", "2"), "1"))
        Assert.assertFalse(DoidEvaluationFunctions.isOfExactDoid(setOf("1", "2"), "2"))
        Assert.assertFalse(DoidEvaluationFunctions.isOfExactDoid(setOf("2"), "1"))
        Assert.assertTrue(DoidEvaluationFunctions.isOfExactDoid(setOf("1"), "1"))
    }

    @Test
    fun canDetermineIfTumorIsOfDoidCombinationType() {
        Assert.assertFalse(DoidEvaluationFunctions.isOfDoidCombinationType(null, setOf("1", "2")))
        Assert.assertFalse(DoidEvaluationFunctions.isOfDoidCombinationType(setOf("1"), setOf("1", "2")))
        Assert.assertTrue(DoidEvaluationFunctions.isOfDoidCombinationType(setOf("1", "2"), setOf("2")))
        Assert.assertTrue(DoidEvaluationFunctions.isOfDoidCombinationType(setOf("1", "2"), setOf("2", "1")))
    }

    @Test
    fun canDetermineIfTumorIsOfExclusiveDoidType() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        Assert.assertFalse(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, null, "child"))
        Assert.assertFalse(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("parent"), "child"))
        Assert.assertTrue(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("child"), "child"))
        Assert.assertTrue(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("child"), "parent"))
        Assert.assertFalse(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("child", "other"), "parent"))
    }

    @Test
    fun canDetermineIfDoidIsExclusiveType() {
        Assert.assertEquals(EvaluationResult.PASS, hasExclusiveTumorTypeOfDoid(MATCH_DOID))
        val firstWarnDoid = WARN_DOIDS.iterator().next()
        Assert.assertEquals(EvaluationResult.WARN, hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid))
        val firstFailDoid = FAIL_DOIDS.iterator().next()
        Assert.assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid, firstFailDoid))
        Assert.assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid("arbitrary doid"))
        Assert.assertEquals(EvaluationResult.FAIL, hasExclusiveTumorTypeOfDoid(MATCH_DOID, "arbitrary doid"))
    }

    @Test
    fun canEvaluateIfPatientHasSpecificDoidCombination() {
        val tumorDoids: Set<String> = setOf("1", "2", "3")
        val set1: Set<String> = setOf("1", "4")
        val set2: Set<String> = setOf("2", "3")
        val set3: Set<String> = setOf("1")
        val combinationSet1: Set<Set<String>> = setOf(set1)
        val combinationSet2: Set<Set<String>> = setOf(set2)
        val combinationSet3: Set<Set<String>> = setOf(set3)
        val combinationSet4: Set<Set<String>> = setOf(set1, set2)
        Assert.assertFalse(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(null, combinationSet1))
        Assert.assertFalse(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet1))
        Assert.assertTrue(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet2))
        Assert.assertTrue(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet3))
        Assert.assertTrue(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet4))
    }

    companion object {
        private const val MATCH_DOID = "1"
        private val FAIL_DOIDS: Set<String> = setOf("2", "3")
        private val WARN_DOIDS: Set<String> = setOf("4", "5")
        private val MATCHING_TEST_MODEL = createTestDoidModelForMatching()
        private fun hasExclusiveTumorTypeOfDoid(vararg tumorDoids: String): EvaluationResult {
            return DoidEvaluationFunctions.evaluateAllDoidsMatchWithFailAndWarns(
                MATCHING_TEST_MODEL,
                setOf(*tumorDoids),
                setOf(MATCH_DOID),
                FAIL_DOIDS,
                WARN_DOIDS
            )
        }

        private fun createTestDoidModelForMatching(): DoidModel {
            val childParentMap: Map<String, String> = (FAIL_DOIDS + WARN_DOIDS).associateWith { MATCH_DOID }
            return TestDoidModelFactory.createWithChildToParentMap(childParentMap)
        }
    }
}