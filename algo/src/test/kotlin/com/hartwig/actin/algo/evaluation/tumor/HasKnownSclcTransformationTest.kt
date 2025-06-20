package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.doid.DoidConstants.SMALL_CELL_LUNG_CANCER_DOIDS
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasKnownSclcTransformationTest {

    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    private val function = HasKnownSclcTransformation(doidModel)

    @Test
    fun `Should be undetermined if tumor is NSCLC and has also small cell doid`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withDoids(
                    SMALL_CELL_LUNG_CANCER_DOIDS.first(),
                    DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID
                )
            )
        )
    }

    @Test
    fun `Should be undetermined if tumor is NSCLC and has small cell component`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoidAndName(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID, "small cell name"))
        )
    }

    @Test
    fun `Should be undetermined if tumor if exact lung carcinoma doid`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_CARCINOMA_DOID)))
    }

    @Test
    fun `Should fail if tumor type not lung cancer`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LIVER_CANCER_DOID))
        )
    }

    @Test
    fun `Should fail if tumor has small cell doid but no NSCLC doid`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(SMALL_CELL_LUNG_CANCER_DOIDS.first()))
        )
    }

    @Test
    fun `Should fail if tumor doids and molecular profile do not indicate a possible small cell transformation`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID))
        )
    }

    @Test
    fun `Should resolve to undetermined if tumor has small cell molecular profile`() {
        val copyNumber = TestCopyNumberFactory.createMinimal().copy(
            gene = "RB1",
            isReportable = true,
            geneRole = GeneRole.TSG,
            proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL)
        )
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val record = base.copy(
            tumor = base.tumor.copy(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)),
            molecularHistory = MolecularTestFactory.withCopyNumber(copyNumber).molecularHistory
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if small cell transformation may have occurred (RB1 inactivation detected)")
    }
}