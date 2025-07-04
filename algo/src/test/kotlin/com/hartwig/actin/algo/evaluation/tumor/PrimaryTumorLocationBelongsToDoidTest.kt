package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PARENT_DOID_1 = "100"
private const val CHILD_DOID_1 = "200"
private const val PARENT_DOID_2 = "300"
private const val CHILD_DOID_2 = "400"

private const val SPECIFIC_QUERY = "specific"
private const val NAME_WITH_SPECIFIC_QUERY = "name with $SPECIFIC_QUERY term"

class PrimaryTumorLocationBelongsToDoidTest {
    private val specificQueryFunction =
        PrimaryTumorLocationBelongsToDoid(simpleDoidModel, setOf(CHILD_DOID_1, CHILD_DOID_2), SPECIFIC_QUERY)

    @Test
    fun `Should evaluate whether tumor doid matches target`() {
        assertResultsForFunction(PrimaryTumorLocationBelongsToDoid(simpleDoidModel, setOf(PARENT_DOID_1, PARENT_DOID_2), null), true)
        assertResultsForFunction(PrimaryTumorLocationBelongsToDoid(simpleDoidModel, setOf(CHILD_DOID_1, CHILD_DOID_2), null), false)
    }

    @Test
    fun `Should evaluate undeterminate main cancer type`() {
        val cancer = "1"
        val stomachCancer = "2"
        val stomachCarcinoma = "3"
        val stomachAdenocarcinoma = "4"
        val stomachLymphoma = "5"
        val esophagusCancer = "6"
        val childToParentMap: Map<String, String> = mapOf(
            stomachAdenocarcinoma to stomachCarcinoma, stomachCarcinoma to stomachCancer,
            stomachLymphoma to stomachCancer, stomachCancer to cancer
        )
        val doidModel: DoidModel = TestDoidModelFactory.createWithMainCancerTypeAndChildToParentMap(stomachCancer, childToParentMap)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, setOf(stomachCarcinoma, esophagusCancer), null)
        assertResultForDoid(EvaluationResult.FAIL, function, "something else")
        assertResultForDoid(EvaluationResult.FAIL, function, cancer)
        assertResultForDoid(EvaluationResult.FAIL, function, stomachLymphoma)
        assertResultForDoid(EvaluationResult.UNDETERMINED, function, stomachCancer)
        assertResultForDoid(EvaluationResult.PASS, function, stomachCarcinoma)
        assertResultForDoid(EvaluationResult.PASS, function, stomachAdenocarcinoma)
        assertResultForDoids(EvaluationResult.PASS, function, setOf("something else", stomachAdenocarcinoma))
        assertResultForDoids(EvaluationResult.PASS, function, setOf(esophagusCancer, stomachAdenocarcinoma))
    }

    @Test
    fun `Should fail when patient has neuroendocrine tumor doid and doid to match is not neuroendocrine`() {
        val pancreaticCancer = "1"
        val pancreaticAdeno = "2"
        val pancreaticNeuroendocrine = "3"
        val ovaryNeuroendocrine = "4"
        val childToParentsMap: Map<String, List<String>> = mapOf(
            pancreaticAdeno to listOf(pancreaticCancer),
            pancreaticNeuroendocrine to listOf(DoidConstants.NEUROENDOCRINE_TUMOR_DOID, pancreaticCancer),
            ovaryNeuroendocrine to listOf(DoidConstants.NEUROENDOCRINE_TUMOR_DOID)
        )
        val doidModel: DoidModel = TestDoidModelFactory.createWithMainCancerTypeAndChildToParentsMap(pancreaticCancer, childToParentsMap)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, setOf(pancreaticAdeno), null)
        assertResultForDoids(EvaluationResult.FAIL, function, setOf(pancreaticCancer, DoidConstants.NEUROENDOCRINE_TUMOR_DOID))
        assertResultForDoids(
            EvaluationResult.UNDETERMINED,
            PrimaryTumorLocationBelongsToDoid(doidModel, setOf(pancreaticNeuroendocrine), null),
            setOf(pancreaticCancer, DoidConstants.NEUROENDOCRINE_TUMOR_DOID)
        )
        assertResultForDoid(EvaluationResult.UNDETERMINED, function, pancreaticCancer)
        assertResultForDoids(
            EvaluationResult.FAIL,
            PrimaryTumorLocationBelongsToDoid(doidModel, setOf(pancreaticAdeno, ovaryNeuroendocrine), null),
            setOf(pancreaticCancer, DoidConstants.NEUROENDOCRINE_TUMOR_DOID)
        )
    }

    @Test
    fun `Should resolve to adeno squamous type`() {
        val mapping = AdenoSquamousMapping(adenoSquamousDoid = "1", squamousDoid = "2", adenoDoid = "3")
        val config = TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping)
        val doidModel = TestDoidModelFactory.createWithDoidManualConfig(config)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, setOf("2", "5"), null)
        assertResultForDoid(EvaluationResult.FAIL, function, "4")
        assertResultForDoid(EvaluationResult.WARN, function, "1")
        assertResultForDoid(EvaluationResult.PASS, function, "2")
        assertResultForDoid(EvaluationResult.PASS, function, "5")
    }

    private fun assertResultsForFunction(function: PrimaryTumorLocationBelongsToDoid, doidToMatchIsParent: Boolean) {
        val expectedResultForParentDoid = if (doidToMatchIsParent) EvaluationResult.PASS else EvaluationResult.FAIL
        assertResultForDoid(expectedResultForParentDoid, function, PARENT_DOID_1)
        assertResultForDoid(expectedResultForParentDoid, function, PARENT_DOID_2)
        assertResultForDoid(EvaluationResult.PASS, function, CHILD_DOID_1)
        assertResultForDoid(EvaluationResult.PASS, function, CHILD_DOID_2)
        assertResultForDoids(expectedResultForParentDoid, function, setOf("10", PARENT_DOID_1))
        assertResultForDoids(EvaluationResult.FAIL, function, setOf("50", "250"))
        assertResultForDoids(EvaluationResult.UNDETERMINED, function, null)
        assertResultForDoids(EvaluationResult.UNDETERMINED, function, emptySet())
        assertResultForDoids(EvaluationResult.PASS, function, setOf(CHILD_DOID_1, CHILD_DOID_2))
    }

    private fun assertResultForDoid(expectedResult: EvaluationResult, function: PrimaryTumorLocationBelongsToDoid, doid: String) {
        assertResultForDoids(expectedResult, function, setOf(doid))
    }

    private fun assertResultForDoids(expectedResult: EvaluationResult, function: PrimaryTumorLocationBelongsToDoid, doids: Set<String>?) {
        assertEvaluation(expectedResult, function.evaluate(TumorTestFactory.withDoids(doids)))
    }

    @Test
    fun `Should show correct fail message`() {
        val function = PrimaryTumorLocationBelongsToDoid(simpleDoidModel, setOf(CHILD_DOID_1, CHILD_DOID_2), null)
        assertThat(
            function.evaluate(TumorTestFactory.withDoids(setOf("50", "250"))).failMessagesStrings()
        ).contains("No child term 1 or child term 2")
    }

    @Test
    fun `Should fail when sub location matches and tumor doid does not match`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            specificQueryFunction.evaluate(TumorTestFactory.withDoidAndName("10", NAME_WITH_SPECIFIC_QUERY))
        )
    }

    @Test
    fun `Should be undetermined when sub location query provided and tumor doids not provided`() {
        assertResultForDoids(EvaluationResult.UNDETERMINED, specificQueryFunction, null)
    }

    @Test
    fun `Should warn when sub query provided and doid match and tumor sub query does not match`() {
        assertEvaluation(
            EvaluationResult.WARN,
            specificQueryFunction.evaluate(TumorTestFactory.withDoidAndName(CHILD_DOID_1, "another"))
        )
    }

    @Test
    fun `Should pass when sub query and doid match`() {
        val pass = specificQueryFunction.evaluate(TumorTestFactory.withDoidAndName(CHILD_DOID_1, NAME_WITH_SPECIFIC_QUERY))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passMessagesStrings()).contains("Tumor belongs to child term 1 with specific request 'specific'")
    }

    companion object {
        private val simpleDoidModel = TestDoidModelFactory.createWithParentChildAndTermPerDoidMaps(
            mapOf(CHILD_DOID_1 to PARENT_DOID_1, CHILD_DOID_2 to PARENT_DOID_2),
            mapOf(
                CHILD_DOID_1 to "child term 1",
                PARENT_DOID_1 to "parent term 1",
                CHILD_DOID_2 to "child term 2",
                PARENT_DOID_2 to "parent term 2"
            ),
        )
    }
}