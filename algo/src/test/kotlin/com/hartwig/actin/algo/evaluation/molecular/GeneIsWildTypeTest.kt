package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCHING_GENE = "GeneA"

class GeneIsWildTypeTest {

    private val function = GeneIsWildType(MATCHING_GENE)

    @Test
    fun `Should pass with no molecular findings`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should fail with reportable high driver variant with protein effect`() {
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
    }

    @Test
    fun `Should warn with reportable high driver variant with no protein effect`() {
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
    }

    @Test
    fun `Should warn with reportable low driver variant`() {
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
    fun `Should warn with specific message for reportable copy number`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withCopyNumber(
                TestCopyNumberFactory.createMinimal().copy(event = "$MATCHING_GENE CN", gene = MATCHING_GENE, isReportable = true))
        )
        
        assertMolecularEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessages).isEqualTo(
            setOf(StaticMessage("Reportable event(s) GeneA CN in GeneA which may potentially be considered wild-type")))
    }
    
    @Test
    fun `Should fail with reportable homozygous disruption with loss of function in TSG`() {
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
    }
    
    @Test
    fun `Should warn with reportable homozygous disruption with no protein effect`() {
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
    }

    @Test
    fun `Should pass with reportable homozygous disruption with in oncogene`() {
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
    fun `Should fail with reportable disruption with loss of function in TSG`() {
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
    }
    
    @Test
    fun `Should warn with reportable disruption with no protein effect in TSG`() {
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
    }

    @Test
    fun `Should pass with Reportable disruption with loss of function in ONCO`() {
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
    fun `Should fail with reportable fusion with gain of function`() {
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
    }

    @Test
    fun `Should warn with reportable fusion with no protein effect`() {
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
    fun `Should warn in case no variant is found and purity is low`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHasSufficientQualityAndPurity(
                    hasSufficientPurity = false,
                    hasSufficientQuality = true
                )
            )
        )
    }

    @Test
    fun `Should pass in case no variant is found and sufficient quality and purity`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHasSufficientQualityAndPurity(
                    hasSufficientPurity = true,
                    hasSufficientQuality = true
                )
            )
        )
    }

    @Test
    fun `Should pass for tested gene having no event in panel`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(
                molecularTests = listOf(
                    TestMolecularFactory.createMinimalPanelTest()
                        .copy(targetSpecification = TestMolecularFactory.panelSpecifications(setOf("ALK")))
                )
            )
        val evaluationResult = GeneIsWildType("ALK").evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.PASS, evaluationResult)
    }

    @Test
    fun `Should be undetermined for gene not tested in panel`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(
                molecularTests = listOf(
                    TestMolecularFactory.createMinimalPanelTest()
                        .copy(targetSpecification = TestMolecularFactory.panelSpecifications(setOf("ALK")))
                )
            )
        val evaluationResult = GeneIsWildType("EGFR").evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluationResult)
    }

    @Test
    fun `Should fail for gene with variant in panels`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(
                molecularTests = listOf(
                    TestMolecularFactory.createMinimalPanelTest().copy(
                        targetSpecification = TestMolecularFactory.panelSpecifications(setOf("ALK")),
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                            variants = listOf(
                                TestVariantFactory.createMinimal()
                                    .copy(
                                        gene = "ALK",
                                        isReportable = true,
                                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                                        driverLikelihood = DriverLikelihood.HIGH
                                    )
                            )
                        )
                    )
                )
            )
        val evaluationResult = GeneIsWildType("ALK").evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluationResult)
    }

    @Test
    fun `Should fail for gene with fusion in panels`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(
                molecularTests = listOf(
                    TestMolecularFactory.createMinimalPanelTest().copy(
                        targetSpecification = TestMolecularFactory.panelSpecifications(setOf("ALK")),
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                            fusions = listOf(
                                TestFusionFactory.createMinimal().copy(
                                    geneEnd = "ALK",
                                    geneStart = "EML4",
                                    isReportable = true,
                                    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                                    driverLikelihood = DriverLikelihood.HIGH
                                )
                            )
                        )
                    )
                )
            )
        val evaluationResult = GeneIsWildType("ALK").evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluationResult)
    }

    @Test
    fun `Should fail when at least one test fails`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord()
            .copy(
                molecularTests = listOf(
                    TestMolecularFactory.createMinimalPanelTest().copy(
                        targetSpecification = TestMolecularFactory.panelSpecifications(setOf("ALK")),
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                            variants = listOf(
                                TestVariantFactory.createMinimal()
                                    .copy(
                                        gene = "ALK",
                                        isReportable = true,
                                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                                        driverLikelihood = DriverLikelihood.HIGH
                                    ),
                                TestVariantFactory.createMinimal()
                                    .copy(
                                        gene = "EGFR",
                                        isReportable = true,
                                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                                        driverLikelihood = DriverLikelihood.HIGH
                                    )
                            )
                        ),
                    ), TestMolecularFactory.createMinimalPanelTest().copy(
                        targetSpecification = TestMolecularFactory.panelSpecifications(setOf("ALK")),
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                            variants = listOf(
                                TestVariantFactory.createMinimal()
                                    .copy(
                                        gene = "KRAS",
                                        isReportable = true,
                                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                                        driverLikelihood = DriverLikelihood.HIGH
                                    ),

                                )
                        )
                    )
                )
            )
        val evaluationResult = GeneIsWildType("ALK").evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.FAIL, evaluationResult)
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Wildtype of gene GeneA undetermined (not tested for at least mutations)")
    }
}