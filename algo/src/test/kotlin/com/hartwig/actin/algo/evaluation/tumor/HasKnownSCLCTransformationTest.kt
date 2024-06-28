package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import org.assertj.core.api.Assertions
import org.junit.Test

class HasKnownSCLCTransformationTest {

    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    private val function = HasKnownSCLCTransformation(doidModel)

    @Test
    fun `Should fail if tumor doids and molecular profile do not indicate a possible small cell transformation`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID))
        )
    }

    @Test
    fun `Should resolve to undetermined if tumor has small cell doid configured`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(HasKnownSCLCTransformation.SMALL_CELL_LUNG_CANCER_DOIDS.iterator().next()))
        )
    }

    @Test
    fun `Should resolve to undetermined if tumor has small cell molecular profile`() {
        val copyNumber = TestCopyNumberFactory.createMinimal().copy(
            gene = "RB1",
            isReportable = true,
            geneRole = GeneRole.TSG,
            proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
            type = CopyNumberType.LOSS
        )
        val evaluation = function.evaluate(MolecularTestFactory.withCopyNumber(copyNumber))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assertions.assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Undetermined small cell transformation (RB1 loss detected)"
        )
    }
}