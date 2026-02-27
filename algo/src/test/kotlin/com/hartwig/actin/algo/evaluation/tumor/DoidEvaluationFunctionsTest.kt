package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoidEvaluationFunctionsTest {

    @Test
    fun `Should determine if tumor has configured doids`() {
        assertThat(DoidEvaluationFunctions.hasConfiguredDoids(null)).isFalse
        assertThat(DoidEvaluationFunctions.hasConfiguredDoids(emptySet())).isFalse
        assertThat(DoidEvaluationFunctions.hasConfiguredDoids(setOf("yes!"))).isTrue
    }

    @Test
    fun `Should determine if tumor is of doid type`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        assertThat(DoidEvaluationFunctions.isOfDoidType(doidModel, null, "child")).isFalse
        assertThat(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("parent"), "child")).isFalse
        assertThat(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("child"), "child")).isTrue
        assertThat(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("child"), "parent")).isTrue
        assertThat(DoidEvaluationFunctions.isOfDoidType(doidModel, setOf("child", "other"), "parent")).isTrue
    }

    @Test
    fun `Should determine if tumor is of least one doid type`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, null, setOf("child"))).isFalse
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, setOf("parent"), setOf("child"))).isFalse
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, setOf("child"), setOf("child", "other"))).isTrue
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, setOf("child"), setOf("parent"))).isTrue
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, setOf("child", "other"), setOf("and another", "parent")))
            .isTrue
    }

    @Test
    fun `Should determine if tumor is of at least one doid term`() {
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("match doid", "match doid term")
        val validDoidTerms: Set<String> = setOf("match doid term")
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, null, validDoidTerms)).isFalse
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, setOf(), validDoidTerms)).isFalse
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, setOf("wrong"), validDoidTerms)).isFalse
        assertThat(DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, setOf("match doid"), validDoidTerms)).isTrue
    }

    @Test
    fun `Should determine if tumor has exact doid`() {
        assertThat(DoidEvaluationFunctions.isOfExactDoid(null, "1")).isFalse
        assertThat(DoidEvaluationFunctions.isOfExactDoid(setOf("1", "2"), "1")).isFalse
        assertThat(DoidEvaluationFunctions.isOfExactDoid(setOf("1", "2"), "2")).isFalse
        assertThat(DoidEvaluationFunctions.isOfExactDoid(setOf("2"), "1")).isFalse
        assertThat(DoidEvaluationFunctions.isOfExactDoid(setOf("1"), "1")).isTrue
    }

    @Test
    fun `Should determine if tumor is of doid combination type`() {
        assertThat(DoidEvaluationFunctions.isOfDoidCombinationType(null, setOf("1", "2"))).isFalse
        assertThat(DoidEvaluationFunctions.isOfDoidCombinationType(setOf("1"), setOf("1", "2"))).isFalse
        assertThat(DoidEvaluationFunctions.isOfDoidCombinationType(setOf("1", "2"), setOf("2"))).isTrue
        assertThat(DoidEvaluationFunctions.isOfDoidCombinationType(setOf("1", "2"), setOf("2", "1"))).isTrue
    }

    @Test
    fun `Should determine if tumor is of exclusive doid type`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        assertThat(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, null, "child")).isFalse
        assertThat(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("parent"), "child")).isFalse
        assertThat(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("child"), "child")).isTrue
        assertThat(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("child"), "parent")).isTrue
        assertThat(DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, setOf("child", "other"), "parent")).isFalse
    }

    @Test
    fun `Should determine if doid is exclusive type`() {
        assertThat(hasExclusiveTumorTypeOfDoid(MATCH_DOID)).isEqualTo(EvaluationResult.PASS)
        val firstWarnDoid = WARN_DOIDS.iterator().next()
        assertThat(hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid)).isEqualTo(EvaluationResult.WARN)
        val firstFailDoid = FAIL_DOIDS.iterator().next()
        assertThat(hasExclusiveTumorTypeOfDoid(MATCH_DOID, firstWarnDoid, firstFailDoid)).isEqualTo(EvaluationResult.FAIL)
        assertThat(hasExclusiveTumorTypeOfDoid("arbitrary doid")).isEqualTo(EvaluationResult.FAIL)
        assertThat(hasExclusiveTumorTypeOfDoid(MATCH_DOID, "arbitrary doid")).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate if patient has specific doid combination`() {
        val tumorDoids = setOf("1", "2", "3")
        val set1 = setOf("1", "4")
        val set2 = setOf("2", "3")
        val set3 = setOf("1")
        val combinationSet1 = setOf(set1)
        val combinationSet2 = setOf(set2)
        val combinationSet3 = setOf(set3)
        val combinationSet4 = setOf(set1, set2)
        assertThat(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(null, combinationSet1)).isFalse
        assertThat(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet1)).isFalse
        assertThat(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet2)).isTrue
        assertThat(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet3)).isTrue
        assertThat(DoidEvaluationFunctions.hasAtLeastOneCombinationOfDoids(tumorDoids, combinationSet4)).isTrue
    }

    companion object {
        private const val MATCH_DOID = "1"
        private val FAIL_DOIDS = setOf("2", "3")
        private val WARN_DOIDS = setOf("4", "5")
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