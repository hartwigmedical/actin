package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory
import org.junit.Test

private const val PARENT_DOID = "100"
private const val CHILD_DOID = "200"

private const val SUB_LOCATION = "specific"

class PrimaryTumorLocationBelongsToDoidTest {
    private val subLocationFunction = PrimaryTumorLocationBelongsToDoid(simpleDoidModel, CHILD_DOID, SUB_LOCATION)

    @Test
    fun `Should evaluate whether tumor doid matches target`() {
        assertResultsForFunction(PrimaryTumorLocationBelongsToDoid(simpleDoidModel, PARENT_DOID, null), true)
        assertResultsForFunction(PrimaryTumorLocationBelongsToDoid(simpleDoidModel, CHILD_DOID, null), false)
    }

    @Test
    fun `Should evaluate undeterminate main cancer type`() {
        val cancer = "1"
        val stomachCancer = "2"
        val stomachCarcinoma = "3"
        val stomachAdenocarcinoma = "4"
        val stomachLymphoma = "5"
        val childToParentMap: Map<String, String> = mapOf(
            stomachAdenocarcinoma to stomachCarcinoma, stomachCarcinoma to stomachCancer,
            stomachLymphoma to stomachCancer, stomachCancer to cancer
        )
        val doidModel: DoidModel = TestDoidModelFactory.createWithMainCancerTypeAndChildToParentMap(stomachCancer, childToParentMap)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, stomachCarcinoma, null)
        assertResultForDoid(EvaluationResult.FAIL, function, "something else")
        assertResultForDoid(EvaluationResult.FAIL, function, cancer)
        assertResultForDoid(EvaluationResult.FAIL, function, stomachLymphoma)
        assertResultForDoid(EvaluationResult.UNDETERMINED, function, stomachCancer)
        assertResultForDoid(EvaluationResult.PASS, function, stomachCarcinoma)
        assertResultForDoid(EvaluationResult.PASS, function, stomachAdenocarcinoma)
        assertResultForDoids(EvaluationResult.PASS, function, setOf("something else", stomachAdenocarcinoma))
    }

    @Test
    fun `Should resolve to adeno squamous type`() {
        val mapping = AdenoSquamousMapping(adenoSquamousDoid = "1", squamousDoid = "2", adenoDoid = "3")
        val config = TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping)
        val doidModel = TestDoidModelFactory.createWithDoidManualConfig(config)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, "2", null)
        assertResultForDoid(EvaluationResult.FAIL, function, "4")
        assertResultForDoid(EvaluationResult.WARN, function, "1")
        assertResultForDoid(EvaluationResult.PASS, function, "2")
    }

    private fun assertResultsForFunction(function: PrimaryTumorLocationBelongsToDoid, doidToMatchIsParent: Boolean) {
        val expectedResultForParentDoid = if (doidToMatchIsParent) EvaluationResult.PASS else EvaluationResult.FAIL
        assertResultForDoid(expectedResultForParentDoid, function, PARENT_DOID)
        assertResultForDoid(EvaluationResult.PASS, function, CHILD_DOID)
        assertResultForDoids(expectedResultForParentDoid, function, setOf("10", PARENT_DOID))
        assertResultForDoids(EvaluationResult.FAIL, function, setOf("50", "250"))
        assertResultForDoids(EvaluationResult.UNDETERMINED, function, null)
        assertResultForDoids(EvaluationResult.UNDETERMINED, function, emptySet())
    }

    private fun assertResultForDoid(expectedResult: EvaluationResult, function: PrimaryTumorLocationBelongsToDoid, doid: String) {
        assertResultForDoids(expectedResult, function, setOf(doid))
    }

    private fun assertResultForDoids(expectedResult: EvaluationResult, function: PrimaryTumorLocationBelongsToDoid, doids: Set<String>?) {
        assertEvaluation(expectedResult, function.evaluate(TumorTestFactory.withDoids(doids)))
    }

    @Test
    fun `Should fail when sub location matches and tumor doid does not match`() {
        assertEvaluation(EvaluationResult.FAIL, subLocationFunction.evaluate(TumorTestFactory.withDoidAndSubLocation("10", SUB_LOCATION)))
    }

    @Test
    fun `Should be undetermined when sub location query provided and tumor doids not provided`() {
        assertResultForDoids(EvaluationResult.UNDETERMINED, subLocationFunction, null)
    }

    @Test
    fun `Should warn when sub location query provided and doid match and tumor sub location is null`() {
        assertResultForDoid(EvaluationResult.WARN, subLocationFunction, CHILD_DOID)
    }

    @Test
    fun `Should warn when sub location query provided and doid match and tumor sub location does not match`() {
        assertEvaluation(
            EvaluationResult.WARN,
            subLocationFunction.evaluate(TumorTestFactory.withDoidAndSubLocation(CHILD_DOID, "another"))
        )
    }

    @Test
    fun `Should pass when sub location and doid match`() {
        assertEvaluation(
            EvaluationResult.PASS,
            subLocationFunction.evaluate(TumorTestFactory.withDoidAndSubLocation(CHILD_DOID, SUB_LOCATION))
        )
    }

    companion object {
        private val simpleDoidModel: DoidModel = TestDoidModelFactory.createWithOneParentChild(PARENT_DOID, CHILD_DOID)
    }
}