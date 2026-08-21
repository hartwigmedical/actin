package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.doid.CuppaDoids
import com.hartwig.actin.doid.CuppaToDoidMapping
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private const val PARENT_DOID_1 = "100"
private const val CHILD_DOID_1 = "200"
private const val PARENT_DOID_2 = "300"
private const val CHILD_DOID_2 = "400"
private const val UNMATCHED_DOID = "50"

private const val SPECIFIC_QUERY = "specific"
private const val NAME_WITH_SPECIFIC_QUERY = "name with $SPECIFIC_QUERY term"

private const val CUPPA_RESULT_CHILD_1 = "child_1"
private const val CUPPA_RESULT_PARENT_1 = "parent_1"
private const val CUPPA_RESULT_CHILD_2_AND_UNMATCHED = "child_2_and_unmatched"
private const val CUPPA_RESULT_PARENT_2_STRICT = "parent_2_strict"

class PrimaryTumorLocationBelongsToDoidTest {

    private val simpleDoidModel = TestDoidModelFactory.createWithParentChildAndTermPerDoidMaps(
        mapOf(CHILD_DOID_1 to PARENT_DOID_1, CHILD_DOID_2 to PARENT_DOID_2),
        mapOf(
            CHILD_DOID_1 to "child term 1",
            PARENT_DOID_1 to "parent term 1",
            CHILD_DOID_2 to "child term 2",
            PARENT_DOID_2 to "parent term 2"
        ),
    )

    private val sarcomaDoidModel = TestDoidModelFactory.createWithParentChildAndTermPerDoidMaps(
        mapOf(DoidConstants.RECTUM_SARCOMA_DOID to DoidConstants.COLORECTAL_CANCER_DOID),
        mapOf(
            DoidConstants.RECTUM_SARCOMA_DOID to "rectum sarcoma",
            DoidConstants.COLORECTAL_CANCER_DOID to "colorectal cancer",
            DoidConstants.SARCOMA_DOID to "sarcoma"
        )
    ).copy(doidManualConfig = TestDoidManualConfigFactory.createWithOneMainCancerDoid(DoidConstants.COLORECTAL_CANCER_DOID))

    private val cuppaToDoidMapping = CuppaToDoidMapping(
        mapOf(
            CUPPA_RESULT_CHILD_1 to CuppaDoids(included = setOf(CHILD_DOID_1)),
            CUPPA_RESULT_PARENT_1 to CuppaDoids(included = setOf(PARENT_DOID_1)),
            CUPPA_RESULT_CHILD_2_AND_UNMATCHED to CuppaDoids(included = setOf(CHILD_DOID_2, UNMATCHED_DOID)),
            CUPPA_RESULT_PARENT_2_STRICT to CuppaDoids(included = setOf(PARENT_DOID_2), excluded = setOf(CHILD_DOID_2)),
        )
    )

    private val parentMatchingFunction =
        PrimaryTumorLocationBelongsToDoid(simpleDoidModel, cuppaToDoidMapping, setOf(PARENT_DOID_1, PARENT_DOID_2), null)
    private val childMatchingFunction =
        PrimaryTumorLocationBelongsToDoid(simpleDoidModel, cuppaToDoidMapping, setOf(CHILD_DOID_1, CHILD_DOID_2), null)
    private val specificQueryFunction =
        PrimaryTumorLocationBelongsToDoid(simpleDoidModel, cuppaToDoidMapping, setOf(CHILD_DOID_1, CHILD_DOID_2), SPECIFIC_QUERY)

    @Test
    fun `Should evaluate whether tumor doid matches target`() {
        assertResultsForFunction(parentMatchingFunction, true)
        assertResultsForFunction(childMatchingFunction, false)
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
        val termPerDoidMap: Map<String, String> = mapOf(stomachCarcinoma to "stomach carcinoma", esophagusCancer to "esophagus cancer", stomachAdenocarcinoma to "stomach adenocarcinoma")
        val doidModel: DoidModel = TestDoidModelFactory.createWithMainCancerTypeAndChildToParentMap(stomachCancer, childToParentMap, termPerDoidMap)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, cuppaToDoidMapping, setOf(stomachCarcinoma, esophagusCancer), null)
        assertResultForDoid(EvaluationResult.FAIL, function, "something else", "No esophagus cancer or stomach carcinoma")
        assertResultForDoid(EvaluationResult.FAIL, function, cancer, "No esophagus cancer or stomach carcinoma")
        assertResultForDoid(EvaluationResult.FAIL, function, stomachLymphoma, "No esophagus cancer or stomach carcinoma")
        assertResultForDoid(EvaluationResult.UNDETERMINED, function, stomachCancer, "Undetermined if stomach carcinoma")
        assertResultForDoid(EvaluationResult.PASS, function, stomachCarcinoma, "Tumor belongs to DOID term(s) stomach carcinoma")
        assertResultForDoid(EvaluationResult.PASS, function, stomachAdenocarcinoma, "Tumor belongs to DOID term(s) stomach carcinoma")
        assertResultForDoids(
            EvaluationResult.PASS, function, setOf("something else", stomachAdenocarcinoma), "Tumor belongs to DOID term(s) stomach carcinoma"
        )
        assertResultForDoids(
            EvaluationResult.PASS, function, setOf(esophagusCancer, stomachAdenocarcinoma), "Tumor belongs to DOID term(s) esophagus cancer and stomach carcinoma"
        )
    }

    @Test
    fun `Should be undetermined when patient has sarcoma doid and doid that is a child of the main cancer type doid to match`() {
        val function = PrimaryTumorLocationBelongsToDoid(
            sarcomaDoidModel, cuppaToDoidMapping, setOf(DoidConstants.COLORECTAL_CANCER_DOID), null
        )
        assertResultForDoids(
            EvaluationResult.UNDETERMINED,
            function,
            setOf(DoidConstants.RECTUM_SARCOMA_DOID, DoidConstants.SARCOMA_DOID),
            "Undetermined if sarcoma is considered colorectal cancer"
        )
    }

    @Test
    fun `Should pass for sarcoma when sarcoma subtype is requested next to main cancer type`() {
        val function = PrimaryTumorLocationBelongsToDoid(
            sarcomaDoidModel, cuppaToDoidMapping, setOf(DoidConstants.COLORECTAL_CANCER_DOID, DoidConstants.RECTUM_SARCOMA_DOID), null
        )
        assertResultForDoids(
            EvaluationResult.PASS,
            function,
            setOf(DoidConstants.RECTUM_SARCOMA_DOID, DoidConstants.SARCOMA_DOID),
            "Tumor belongs to DOID term(s) colorectal cancer and rectum sarcoma"
        )
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
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, cuppaToDoidMapping, setOf(pancreaticAdeno), null)
        assertResultForDoids(EvaluationResult.FAIL, function, setOf(pancreaticCancer, DoidConstants.NEUROENDOCRINE_TUMOR_DOID), "No ")
        assertResultForDoids(
            EvaluationResult.UNDETERMINED,
            PrimaryTumorLocationBelongsToDoid(doidModel, cuppaToDoidMapping, setOf(pancreaticNeuroendocrine), null),
            setOf(pancreaticCancer, DoidConstants.NEUROENDOCRINE_TUMOR_DOID),
            "Undetermined if "
        )
        assertResultForDoid(EvaluationResult.UNDETERMINED, function, pancreaticCancer, "Undetermined if ")
        assertResultForDoids(
            EvaluationResult.FAIL,
            PrimaryTumorLocationBelongsToDoid(doidModel, cuppaToDoidMapping, setOf(pancreaticAdeno, ovaryNeuroendocrine), null),
            setOf(pancreaticCancer, DoidConstants.NEUROENDOCRINE_TUMOR_DOID),
            "No "
        )
    }

    @Test
    fun `Should resolve to adeno squamous type`() {
        val mapping = AdenoSquamousMapping(adenoSquamousDoid = "1", squamousDoid = "2", adenoDoid = "3")
        val config = TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping)
        val doidModel = TestDoidModelFactory.createWithDoidManualConfig(config)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, cuppaToDoidMapping, setOf("2", "5"), null)
        assertResultForDoid(EvaluationResult.FAIL, function, "4", "No ")
        assertResultForDoid(EvaluationResult.WARN, function, "1", "Unclear if tumor type is considered ")
        assertResultForDoid(EvaluationResult.PASS, function, "2", "Tumor belongs to DOID term(s) ")
        assertResultForDoid(EvaluationResult.PASS, function, "5", "Tumor belongs to DOID term(s) ")
    }

    private fun assertResultsForFunction(function: PrimaryTumorLocationBelongsToDoid, doidToMatchIsParent: Boolean) {
        val expectedResultForParentDoid = if (doidToMatchIsParent) EvaluationResult.PASS else EvaluationResult.FAIL
        val term1 = if (doidToMatchIsParent) "parent term 1" else "child term 1"
        val term2 = if (doidToMatchIsParent) "parent term 2" else "child term 2"
        val noMatchMessage = "No $term1 or $term2"
        val messageForParentDoid1 = if (doidToMatchIsParent) "Tumor belongs to DOID term(s) $term1" else noMatchMessage
        val messageForParentDoid2 = if (doidToMatchIsParent) "Tumor belongs to DOID term(s) $term2" else noMatchMessage
        assertResultForDoid(expectedResultForParentDoid, function, PARENT_DOID_1, messageForParentDoid1)
        assertResultForDoid(expectedResultForParentDoid, function, PARENT_DOID_2, messageForParentDoid2)
        assertResultForDoid(EvaluationResult.PASS, function, CHILD_DOID_1, "Tumor belongs to DOID term(s) $term1")
        assertResultForDoid(EvaluationResult.PASS, function, CHILD_DOID_2, "Tumor belongs to DOID term(s) $term2")
        assertResultForDoids(expectedResultForParentDoid, function, setOf("10", PARENT_DOID_1), messageForParentDoid1)
        assertResultForDoids(EvaluationResult.FAIL, function, setOf(UNMATCHED_DOID, "250"), noMatchMessage)
        assertResultForDoids(EvaluationResult.UNDETERMINED, function, null, "Unknown tumor type")
        assertResultForDoids(EvaluationResult.UNDETERMINED, function, emptySet(), "Unknown tumor type")
        assertResultForDoids(
            EvaluationResult.PASS, function, setOf(CHILD_DOID_1, CHILD_DOID_2), "Tumor belongs to DOID term(s) $term1 and $term2"
        )

        assertResultForCUP(
            EvaluationResult.WARN, function, CUPPA_RESULT_CHILD_1, 0.95, "Tumor type unknown but CUPPA predicts $CUPPA_RESULT_CHILD_1 (95%)"
        )
        assertResultForCUP(
            EvaluationResult.WARN,
            function,
            CUPPA_RESULT_PARENT_1,
            0.95,
            "Tumor type unknown but CUPPA predicts $CUPPA_RESULT_PARENT_1 (95%)"
        )
        assertResultForCUP(
            EvaluationResult.WARN,
            function,
            CUPPA_RESULT_CHILD_2_AND_UNMATCHED,
            0.95,
            "Tumor type unknown but CUPPA predicts $CUPPA_RESULT_CHILD_2_AND_UNMATCHED (95%)"
        )
        val resultForStrictCUP = if (doidToMatchIsParent) EvaluationResult.WARN else EvaluationResult.FAIL
        val messageForStrictCUP =
            if (doidToMatchIsParent) "Tumor type unknown but CUPPA predicts $CUPPA_RESULT_PARENT_2_STRICT (95%)" else noMatchMessage
        assertResultForCUP(resultForStrictCUP, function, CUPPA_RESULT_PARENT_2_STRICT, 0.95, messageForStrictCUP)
        assertResultForCUP(EvaluationResult.FAIL, function, CUPPA_RESULT_CHILD_1, 0.5, noMatchMessage)
    }

    private fun assertResultForDoid(
        expectedResult: EvaluationResult, function: PrimaryTumorLocationBelongsToDoid, doid: String, expectedMessage: String
    ) {
        assertResultForDoids(expectedResult, function, setOf(doid), expectedMessage)
    }

    private fun assertResultForDoids(
        expectedResult: EvaluationResult,
        function: PrimaryTumorLocationBelongsToDoid,
        doids: Set<String>?,
        expectedMessage: String
    ) {
        assertEvaluation(expectedResult, function.evaluate(TumorTestFactory.withDoids(doids)), expectedMessage)
    }

    private fun assertResultForCUP(
        expectedResult: EvaluationResult,
        function: PrimaryTumorLocationBelongsToDoid,
        predictionCancerType: String,
        predictionLikelihood: Double,
        expectedMessage: String
    ) {
        assertEvaluation(
            expectedResult,
            function.evaluate(TumorTestFactory.withCupAndCuppaPrediction(predictionCancerType, predictionLikelihood)),
            expectedMessage
        )
    }

    @Test
    fun `Should show correct fail message`() {
        assertThat(
            childMatchingFunction.evaluate(TumorTestFactory.withDoids(setOf(UNMATCHED_DOID, "250"))).failMessagesStrings()
        ).contains("No child term 1 or child term 2")
    }

    @Test
    fun `Should fail when sub location matches and tumor doid does not match`() {
        assertEvaluation(
            EvaluationResult.FAIL, specificQueryFunction.evaluate(TumorTestFactory.withDoidAndName("10", NAME_WITH_SPECIFIC_QUERY)),
            "No child term 1 or child term 2"
        )
    }

    @Test
    fun `Should be undetermined when sub location query provided and tumor doids not provided`() {
        assertResultForDoids(EvaluationResult.UNDETERMINED, specificQueryFunction, null, "Unknown tumor type")
    }

    @Test
    fun `Should warn when sub query provided and doid match and tumor sub query does not match`() {
        assertEvaluation(
            EvaluationResult.WARN,
            specificQueryFunction.evaluate(TumorTestFactory.withDoidAndName(CHILD_DOID_1, "another")),
            "Tumor belongs to child term 1 but undetermined if 'specific'"
        )
    }

    @Test
    fun `Should pass when sub query and doid match`() {
        val pass = specificQueryFunction.evaluate(TumorTestFactory.withDoidAndName(CHILD_DOID_1, NAME_WITH_SPECIFIC_QUERY))
        assertEvaluation(EvaluationResult.PASS, pass, "Tumor belongs to child term 1 with specific request 'specific'")
        assertThat(pass.passMessagesStrings()).contains("Tumor belongs to child term 1 with specific request 'specific'")
    }

    @Test
    fun `Should throw an exception when encountering an unknown CUPPA output`() {
        assertThrows(IllegalArgumentException::class.java) {
            parentMatchingFunction.evaluate(TumorTestFactory.withCupAndCuppaPrediction("unknown", 0.95))
        }
    }

    @Test
    fun `Should warn for CUP tumor with conclusive CUPPA prediction matching parent doid`() {
        val function =
            PrimaryTumorLocationBelongsToDoid(simpleDoidModel, cuppaToDoidMapping, setOf(PARENT_DOID_1, PARENT_DOID_2), null)
        val warn = function.evaluate(TumorTestFactory.withCupAndCuppaPrediction(CUPPA_RESULT_CHILD_1, 0.95))
        assertThat(warn.warnMessagesStrings()).containsExactly("Tumor type unknown but CUPPA predicts $CUPPA_RESULT_CHILD_1 (95%)")
    }

    @Test
    fun `Should warn for CUP tumor with conclusive CUPPA prediction matching child doid`() {
        val warn = childMatchingFunction.evaluate(TumorTestFactory.withCupAndCuppaPrediction(CUPPA_RESULT_PARENT_1, 0.95))
        assertThat(warn.warnMessagesStrings()).containsExactly("Tumor type unknown but CUPPA predicts $CUPPA_RESULT_PARENT_1 (95%)")
    }

    @Test
    fun `Should fail for CUP tumor with conclusive CUPPA prediction matching excluded doid`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            childMatchingFunction.evaluate(TumorTestFactory.withCupAndCuppaPrediction(CUPPA_RESULT_PARENT_2_STRICT, 0.95)),
            "No child term 1 or child term 2"
        )
    }

    @Test
    fun `Should fail when CUPPA matches but sub location query provided`() {
        val function =
            PrimaryTumorLocationBelongsToDoid(simpleDoidModel, cuppaToDoidMapping, setOf(PARENT_DOID_1, PARENT_DOID_2), SPECIFIC_QUERY)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withCupAndCuppaPrediction(CUPPA_RESULT_CHILD_1, 0.95)),
            "No parent term 1 or parent term 2"
        )
    }

    @Test
    fun `Should fail when CUPPA prediction likelihood is below the threshold`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            parentMatchingFunction.evaluate(TumorTestFactory.withCupAndCuppaPrediction(CUPPA_RESULT_CHILD_1, 0.5)),
            "No parent term 1 or parent term 2"
        )
    }

    @Test
    fun `Should fail when CUPPA matches but the tumor is not a CUP`() {
        val record = TumorTestFactory.withDoidAndName(UNMATCHED_DOID, "another").copy(
            molecularTests = listOf(TestMolecularFactory.createMinimalWholeGenomeTest().run {
                copy(
                    characteristics = characteristics.copy(
                        predictedTumorOrigin = TestMolecularFactory.createHighConfidenceCupPrediction()
                    )
                )
            })
        )
        val function = parentMatchingFunction
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record), "No parent term 1 or parent term 2")
    }

    @Test
    fun `Should fail for CUP tumor without molecular data`() {
        val record = TumorTestFactory.withDoidAndName(DoidConstants.CANCER_DOID, "Cancer (CUP)")
        val function = parentMatchingFunction
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record), "No parent term 1 or parent term 2")
    }

}