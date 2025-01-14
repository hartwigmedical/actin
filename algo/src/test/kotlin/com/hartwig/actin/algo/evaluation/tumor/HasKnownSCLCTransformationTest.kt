package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.doid.DoidConstants.SMALL_CELL_LUNG_CANCER_DOIDS
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasKnownSCLCTransformationTest {

    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    private val function = HasKnownSCLCTransformation(doidModel)

    @Test

    fun `Should fail if tumor type not NSCLC`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LIVER_CANCER_DOID))
        )
    }

    @Test
    fun `Should fail if tumor doids and molecular profile do not indicate a possible small cell transformation`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID))
        )
    }

    @Test
    fun `Should fail if tumor has small cell doid but no NSCLC doid`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(SMALL_CELL_LUNG_CANCER_DOIDS.iterator().next()))
        )
    }

    @Test
    fun `Should resolve to undetermined if tumor has both NSCLC and small cell doid configured`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(
                SMALL_CELL_LUNG_CANCER_DOIDS.iterator().next(),
                DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if tumor is NSCLC and small cell or mixed in tumor extra details`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoidAndDetails(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID, "small cell"))
        )
    }

    @Test
    fun `Should fail if tumor is NSCLC and non-small cell in tumor extra details`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoidAndDetails(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID, "non-small cell"))
        )
    }

    @Test
    fun `Should resolve to undetermined if tumor has small cell molecular profile`() {
        val copyNumber = TestCopyNumberFactory.createMinimal().copy(
            gene = "RB1",
            isReportable = true,
            geneRole = GeneRole.TSG,
            proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
        )
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val record = base.copy(
            tumor = base.tumor.copy(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)),
            molecularHistory = MolecularTestFactory.withCopyNumber(copyNumber).molecularHistory
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("Small cell transformation undetermined (RB1 loss detected)")
    }
}