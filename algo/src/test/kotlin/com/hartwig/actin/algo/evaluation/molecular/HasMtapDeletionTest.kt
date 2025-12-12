package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import org.junit.Test

private const val MTAP = "MTAP"
private const val CDKN2A = "CDKN2A"

class HasMtapDeletionTest {

    private val function = HasMtapDeletion()
    private val mtapDel = TestCopyNumberFactory.createMinimal().copy(
        gene = MTAP,
        isReportable = true,
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_DEL)
    )
    private val cdkn2aDel = mtapDel.copy(gene = CDKN2A)
    private val cdkn2aTarget = CDKN2A to listOf(MolecularTestTarget.DELETION)
    private val testsMtap = PanelTargetSpecification(mapOf(MTAP to listOf(MolecularTestTarget.DELETION), cdkn2aTarget))
    private val testsCdkn2aOnly = PanelTargetSpecification(mapOf(cdkn2aTarget))
    private val panel = TestMolecularFactory.createMinimalPanelTest()
    
    @Test
    fun `Should pass when MTAP is deleted`() {
        val test = panel.copy(
            targetSpecification = testsMtap,
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(copyNumbers = listOf(mtapDel))
        )
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMolecularTests(listOf(test))))
    }

    @Test
    fun `Should fail when CDKN2A is deleted but MTAP is also tested and not deleted`() {
        val test = panel.copy(
            targetSpecification = testsMtap,
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(copyNumbers = listOf(cdkn2aDel))
        )
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTests(listOf(test))))
    }

    @Test
    fun `Should warn when CDKN2A is deleted and MTAP is not tested`() {
        val test = panel.copy(
            targetSpecification = testsCdkn2aOnly,
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(copyNumbers = listOf(cdkn2aDel))
        )
        assertEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withMolecularTests(listOf(test))))
    }

    @Test
    fun `Should evaluate to undetermined when MTAP deletion is not tested and CDKN2A is not deleted`() {
        val test = panel.copy(
            targetSpecification = testsCdkn2aOnly,
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(copyNumbers = emptyList())
        )
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(listOf(test))))
    }

    @Test
    fun `Should evaluate to undetermined for no molecular tests`() {
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList())))
    }

    @Test
    fun `Should evaluate to undetermined when MTAP and CDKN2A are both not tested`() {
        val test = panel.copy(
            targetSpecification = PanelTargetSpecification(emptyMap()),
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(copyNumbers = emptyList())
        )
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(listOf(test))))
    }
}