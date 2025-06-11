package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

const val OTHER_DOID = "other doid"

class TumorEvaluationFunctionsTest {

    private val doidModel =
        TestDoidModelFactory.createWithTwoDoidsAndTerms(
            listOf(
                DoidConstants.SMALL_CELL_CANCER_DOIDS.first(),
                DoidConstants.NEUROENDOCRINE_DOIDS.first(),
            ), listOf("Small cell cancer", "Neuroendocrine cancer")
        )
    private val smallCellDoids = setOf(DoidConstants.SMALL_CELL_CANCER_DOIDS.first(), OTHER_DOID)
    private val neuroendocrineDoids = setOf(DoidConstants.NEUROENDOCRINE_DOIDS.first(), OTHER_DOID)
    private val otherDoids = setOf(OTHER_DOID)
    private val otherName = "name"

    @Test
    fun `Should return true if tumor has small cell doid`() {
        assertThat(TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, smallCellDoids, otherName)).isTrue()
    }

    @Test
    fun `Should return true if tumor has small cell name`() {
        assertThat(
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(
                doidModel,
                otherDoids,
                "name ${TumorTermConstants.SMALL_CELL_TERMS.first()}"
            )
        ).isTrue()
    }

    @Test
    fun `Should return false if tumor has non-small cell name`() {
        assertThat(
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(
                doidModel,
                otherDoids,
                "name ${TumorTermConstants.NON_SMALL_CELL_TERMS.first()}"
            )
        ).isFalse()
    }

    @Test
    fun `Should return false if no small cell DOID or name`() {
        assertThat(
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(
                doidModel,
                otherDoids,
                otherName
            )
        ).isFalse()
    }

    @Test
    fun `Should return true if tumor has neuroendocrine doid`() {
        assertThat(TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, neuroendocrineDoids, otherName)).isTrue()
    }

    @Test
    fun `Should return true if tumor has neuroendocrine doid term`() {
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(OTHER_DOID, "NeuroendOcrine carcinoma")
        assertThat(TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, setOf(OTHER_DOID), otherName)).isTrue
    }

    @Test
    fun `Should return true if tumor has neuroendocrine name`() {
        assertThat(
            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(
                doidModel,
                otherDoids,
                "name ${TumorTermConstants.NEUROENDOCRINE_TERMS.first()}"
            )
        ).isTrue()
    }

    @Test
    fun `Should return false if no neuroendocrine DOID or name`() {
        assertThat(
            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(
                doidModel,
                otherDoids,
                otherName
            )
        ).isFalse()
    }

    @Test
    fun `Should return true if tumor is CUP`() {
        assertThat(TumorEvaluationFunctions.hasCancerOfUnknownPrimary("some ${TumorTermConstants.CUP_TERM} tumor")).isTrue()
    }

    @Test
    fun `Should return false if tumor is no CUP`() {
        assertThat(TumorEvaluationFunctions.hasCancerOfUnknownPrimary("some other tumor")).isFalse()
    }

    @Test
    fun `Should return false if patient does not have peritoneal metastases`() {
        listOf("retroperitoneal lesions", "metastases in subperitoneal region", "Lymph node").forEach { lesion ->
            val tumor = TumorTestFactory.withOtherLesions(listOf(lesion)).tumor
            assertThat(TumorEvaluationFunctions.hasPeritonealMetastases(tumor)).isFalse
        }
    }

    @Test
    fun `Should return true if patient does have peritoneal metastases`() {
        listOf(
            "Abdominal lesion located in Peritoneum", "Multiple depositions abdominal and peritoneal", "intraperitoneal"
        ).forEach { lesion ->
            val tumor = TumorTestFactory.withOtherLesions(listOf(lesion)).tumor
            assertThat(TumorEvaluationFunctions.hasPeritonealMetastases(tumor)).isTrue
        }
    }

    @Test
    fun `Should return null if patient tumor details are unknown`() {
        assertThat(TumorEvaluationFunctions.hasPeritonealMetastases(TumorDetails())).isNull()
    }
}