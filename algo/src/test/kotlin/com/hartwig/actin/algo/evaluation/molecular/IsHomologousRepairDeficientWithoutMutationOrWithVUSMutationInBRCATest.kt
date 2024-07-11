package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInBRCATest {
    private val function = IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInBRCA()

    @Test
    fun `Should fail when HRD status unknown and no reportable drivers in HR genes`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null, hrdVariant()))
        )
    }

    @Test
    fun `Should fail when HRD with loss of BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndLoss(
                    true,
                    TestCopyNumberFactory.createMinimal()
                        .copy(type = CopyNumberType.LOSS, gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH)
                )
            )
        )
    }

    @Test
    fun `Should fail when HRD with BRCA1 hotspot`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true, hrdVariant(isReportable = true, isHotspot = true)
                )
            )
        )
    }

    @Test
    fun `Should fail when no HRD`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(false, hrdVariant()))
        )
    }

    @Test
    fun `Should be undetermined when HRD status unknown but with drivers in HR genes`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null, hrdVariant(isReportable = true))
            )
        )
    }

    @Test
    fun `Should warn when HRD and only a non reportable mutation in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true, hrdVariant()))
        )
    }

    @Test
    fun `Should warn when HRD and non-hotspot biallelic high driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    hrdVariant(isReportable = true, isBiallelic = true, driverLikelihood = DriverLikelihood.HIGH)
                )
            )
        )
    }

    @Test
    fun `Should warn when HRD and non-hotspot biallelic low driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    hrdVariant(isReportable = true, isBiallelic = true)
                )
            )
        )
    }

    @Test
    fun `Should warn when HRD and non-hotspot non-biallelic high driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    hrdVariant(isReportable = true, driverLikelihood = DriverLikelihood.HIGH)
                )
            )
        )
    }

    @Test
    fun `Should warn when HRD and non-hotspot non-biallelic low driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    hrdVariant(isReportable = true)
                )
            )
        )
    }

    @Test
    fun `Should ignore variants with allelic status is unknown in BRCA1`() {
        val result = function.evaluate(
            MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                true,
                TestVariantFactory.createMinimal().copy(gene = "BRCA1", isReportable = true, isHotspot = true)
            )
        )
        assertEvaluation(
            EvaluationResult.WARN,
            result
        )
        assertThat(result.warnSpecificMessages).containsExactly("Homologous repair deficiency (HRD) status detected, without drivers in HR genes")
    }

    @Test
    fun `Should warn when HRD and disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal()
                        .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true)
                )
            )
        )
    }

    @Test
    fun `Should warn when HRD and only non-biallelic drivers in HR genes`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true, hrdVariant(gene = "RAD51C", true)
                )
            )
        )
    }

    @Test
    fun `Should warn when HRD and only a non-homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal()
                        .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true)
                )
            )
        )
    }

    @Test
    fun `Should warn when HRD and no detected drivers in HR genes`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true, hrdVariant())
            )
        )
    }

    @Test
    fun `Should warn when HRD and biallelic RAD51C hotspot and non-homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariantAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal()
                        .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true),
                    hrdVariant("RAD51C", true, true, true)
                )
            )
        )
    }

    @Test
    fun `Should warn when HRD and homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = "BRCA1")
                )
            )
        )
    }

    @Test
    fun `Should pass when HRD and biallelic RAD51C hotspot and no BRCA1 variant`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true, hrdVariant("RAD51C", true, true, true)
                )
            )
        )
    }

    private fun hrdVariant(
        gene: String = "BRCA1",
        isReportable: Boolean = false,
        isBiallelic: Boolean = false,
        isHotspot: Boolean = false,
        driverLikelihood: DriverLikelihood = DriverLikelihood.LOW,
    ): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = gene,
            isReportable = isReportable,
            isHotspot = isHotspot,
            driverLikelihood = driverLikelihood,
            extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = isBiallelic)
        )
    }
}