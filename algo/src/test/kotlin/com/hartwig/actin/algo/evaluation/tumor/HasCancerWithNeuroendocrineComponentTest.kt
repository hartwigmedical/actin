package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasCancerWithNeuroendocrineComponentTest {

    private val function = HasCancerWithNeuroendocrineComponent(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun `Should evaluate to undetermined if no tumor doids configured`() {
        val tumorDetails = TumorTestFactory.withDoids(emptySet())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if tumor has neuroendocrine component`() {
        val tumorDetails = TumorTestFactory.withDoids(setOf(DoidConstants.NEUROENDOCRINE_DOIDS.first()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }


    @Test
    fun `Should be undetermined if tumor has small cell component`() {
        val tumorDetails = TumorTestFactory.withDoids(DoidConstants.SMALL_CELL_CANCER_DOIDS.first())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should be undetermined if molecular profile matches neuroendocrine`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(createWithNeuroendocrineProfile()))
    }

    @Test
    fun `Should fail if tumor is of other type than neuroendocrine cell`() {
        val tumorDetails = TumorTestFactory.withDoidAndName("wrong doid", "wrong name")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumorDetails))
    }

    private fun createWithNeuroendocrineProfile(): PatientRecord {
        val baseMolecular = TestMolecularFactory.createMinimalWholeGenomeTest()
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularTests = MolecularHistory(
                listOf(
                    baseMolecular.copy(
                        drivers = baseMolecular.drivers.copy(
                            copyNumbers = listOf(
                                TestCopyNumberFactory.createMinimal().copy(
                                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL),
                                    isReportable = true,
                                    gene = "TP53"
                                )
                            ),
                            homozygousDisruptions = listOf(
                                TestHomozygousDisruptionFactory.createMinimal().copy(isReportable = true, gene = "RB1")
                            )
                        )
                    )
                )
            )
        )
    }
}