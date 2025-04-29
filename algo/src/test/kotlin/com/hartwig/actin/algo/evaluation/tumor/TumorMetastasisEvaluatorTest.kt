package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val METASTASIS_TYPE = TumorDetails.BONE
private const val CANCER_DOID = "100"
private const val REQUESTED_CHILD_CANCER_DOID = DoidConstants.BONE_CANCER_DOID
private const val OTHER_CHILD_CANCER_DOID = "300"

class TumorMetastasisEvaluatorTest {

    @Test
    fun `Should pass when hasLesions is true and tumor is not a primary tumor`() {
        val pass = TumorMetastasisEvaluator.evaluate(true, false, METASTASIS_TYPE, setOf(OTHER_CHILD_CANCER_DOID), simpleDoidModel)
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passMessages).containsExactly("Has bone metastases")
    }

    @Test
    fun `Should be undetermined when hasLesions is true and tumor is a primary tumor`() {
        val undetermined = TumorMetastasisEvaluator.evaluate(true, false, METASTASIS_TYPE, setOf(REQUESTED_CHILD_CANCER_DOID), simpleDoidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).containsExactly("Has bone lesions but unsure if considered metastases because of primary bone cancer")
    }

    @Test
    fun `Should warn when hasSuspectedLesions is true and tumor is not a primary tumor`() {
        listOf(false, null).forEach { hasKnownLesion ->
            val warn = TumorMetastasisEvaluator.evaluate(hasKnownLesion, true, METASTASIS_TYPE, setOf(OTHER_CHILD_CANCER_DOID), simpleDoidModel)
            assertEvaluation(EvaluationResult.WARN, warn)
            assertThat(warn.warnMessages).containsExactly("Has suspected bone metastases")
        }
    }

    @Test
    fun `Should be undetermined when hasSuspectedLesions is true and tumor is a primary tumor`() {
        listOf(false, null).forEach { hasKnownLesion ->
            val undetermined = TumorMetastasisEvaluator.evaluate(hasKnownLesion, true, METASTASIS_TYPE, setOf(REQUESTED_CHILD_CANCER_DOID), simpleDoidModel)
            assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
            assertThat(undetermined.undeterminedMessages).containsExactly("Has suspected bone lesions but unsure if considered metastases because of primary bone cancer")
        }
    }

    @Test
    fun `Should be undetermined when hasLesions is null and tumor is a primary tumor`() {
        val undetermined =
            TumorMetastasisEvaluator.evaluate(null, null, METASTASIS_TYPE, setOf(REQUESTED_CHILD_CANCER_DOID), simpleDoidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).contains("Has primary bone cancer but undetermined if patient may have bone metastases")
    }

    @Test
    fun `Should be undetermined when hasLesions is null`() {
        val undetermined =
            TumorMetastasisEvaluator.evaluate(null, null, METASTASIS_TYPE, setOf(OTHER_CHILD_CANCER_DOID), simpleDoidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).contains("Missing bone metastasis data")
    }

    @Test
    fun `Should fail when hasLesions and hasSuspectedLesions is false`() {
        val fail = TumorMetastasisEvaluator.evaluate(false, false, METASTASIS_TYPE, setOf(REQUESTED_CHILD_CANCER_DOID), simpleDoidModel)
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failMessages).contains("No bone metastases")
    }

    companion object {
        private val simpleDoidModel = TestDoidModelFactory.createWithChildToParentMap(
            mapOf(REQUESTED_CHILD_CANCER_DOID to CANCER_DOID, OTHER_CHILD_CANCER_DOID to CANCER_DOID)
        )
    }
}