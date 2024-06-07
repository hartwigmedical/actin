package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import org.junit.Test

private const val MATCHING_GENE = "gene A"

class GeneIsWildTypeTest {
    private val function = GeneIsWildType(MATCHING_GENE)

    @Test
    fun `Should evaluate variants`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        proteinEffect = ProteinEffect.NO_EFFECT
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.LOW,
                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate copy numbers`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    TestCopyNumberFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.NO_EFFECT
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    TestCopyNumberFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate homozygous disruptions`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
                        geneRole = GeneRole.TSG
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.NO_EFFECT,
                        geneRole = GeneRole.TSG
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
                        geneRole = GeneRole.ONCO
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate disruptions`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
                        geneRole = GeneRole.TSG
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.NO_EFFECT,
                        geneRole = GeneRole.TSG
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
                        geneRole = GeneRole.ONCO
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate fusions`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal().copy(
                        geneStart = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal().copy(
                        geneEnd = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal().copy(
                        geneStart = MATCHING_GENE,
                        isReportable = true,
                        proteinEffect = ProteinEffect.NO_EFFECT
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for tested gene having no event in panel `() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistoryFactory.withEmptyArcherPanel())
        val evaluationResult = GeneIsWildType("ALK").evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.PASS, evaluationResult)
    }

    @Test
    fun `Should be undetermined for gene not tested in panel`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistoryFactory.withEmptyArcherPanel())
        val evaluationResult = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluationResult)
    }

    @Test
    fun `Should fail for gene with variant in archer panels`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistoryFactory.withArcherVariant(MATCHING_GENE, "c.1234A>T"))

        val evaluationResult = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluationResult)
    }

    @Test
    fun `Should fail for gene with fusion in archer panels`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(molecularHistory = MolecularHistoryFactory.withArcherFusion(MATCHING_GENE))

        val evaluationResult = function.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluationResult)
    }
}