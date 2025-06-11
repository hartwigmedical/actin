package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IsHomologousRecombinationDeficientWithoutMutationInGenesXTest {
    private val genesToFind = setOf("BRCA1", "BRCA2")
    private val function = IsHomologousRecombinationDeficientWithoutMutationInGenesX(genesToFind)

    @Test
    fun `Should fail when HRD status unknown and no reportable drivers in HR genes`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withVariant(hrdVariant())))
    }

    @Test
    fun `Should fail when HRD with deletion of BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDeletion(
                    true,
                    TestCopyNumberFactory.createMinimal()
                        .copy(
                            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL),
                            gene = "BRCA1",
                            driverLikelihood = DriverLikelihood.HIGH
                        )
                )
            )
        )
    }

    @Test
    fun `Should fail when HRD with BRCA1 cancer-associated variant`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true, hrdVariant(isReportable = true, isCancerAssociatedVariant = true, driverLikelihood = DriverLikelihood.HIGH)
                )
            )
        )
    }

    @Test
    fun `Should fail when no HRD`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHomologousRecombinationAndVariant(false, hrdVariant()))
        )
    }

    @Test
    fun `Should be undetermined when HRD status unknown but with drivers in HR genes`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withVariant(hrdVariant(gene = "RAD51C", isReportable = true)))
        )
    }

    @Test
    fun `Should warn when HRD and only a non reportable mutation in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHomologousRecombinationAndVariant(true, hrdVariant()))
        )
    }

    @Test
    fun `Should fail when HRD and non-cancer-associated variant biallelic high driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true, isBiallelic = true, driverLikelihood = DriverLikelihood.HIGH)
                )
            )
        )
    }

    @Test
    fun `Should fail when HRD and non-cancer-associated variant biallelic low driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true, isBiallelic = true)
                )
            )
        )
    }

    @Test
    fun `Should fail when HRD and non-cancer-associated variant non-biallelic high driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true, driverLikelihood = DriverLikelihood.HIGH)
                )
            )
        )
    }

    @Test
    fun `Should fail when HRD and non-cancer-associated variant non-biallelic low driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true)
                )
            )
        )
    }

    @Test
    fun `Should ignore variants with allelic status is unknown in BRCA1`() {
        val result = function.evaluate(
            MolecularTestFactory.withHomologousRecombinationAndVariant(
                true,
                TestVariantFactory.createMinimal().copy(gene = "BRCA1", isReportable = true, isCancerAssociatedVariant = true)
            )
        )
        assertEvaluation(EvaluationResult.WARN, result)
        assertThat(result.warnMessagesStrings()).containsExactly("Tumor is HRD but without drivers in HR genes")
    }

    @Test
    fun `Should fail when HRD and disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDisruption(
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
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true, hrdVariant(gene = "RAD51C", true)
                )
            )
        )
    }

    @Test
    fun `Should fail when HRD and only a non-homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDisruption(
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
                MolecularTestFactory.withHomologousRecombinationAndVariant(true, hrdVariant())
            )
        )
    }

    @Test
    fun `Should fail when HRD and biallelic RAD51C cancer-associated variant and non-homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariantAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal()
                        .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true),
                    hrdVariant("RAD51C", true, true, true)
                )
            )
        )
    }

    @Test
    fun `Should fail when HRD and homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = "BRCA1")
                )
            )
        )
    }

    @Test
    fun `Should pass when HRD and biallelic RAD51C cancer-associated variant and no BRCA1 variant`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true, hrdVariant("RAD51C", true, true, true, DriverLikelihood.HIGH)
                )
            )
        )
    }

    private fun hrdVariant(
        gene: String = "BRCA1",
        isReportable: Boolean = false,
        isBiallelic: Boolean = false,
        isCancerAssociatedVariant: Boolean = false,
        driverLikelihood: DriverLikelihood = DriverLikelihood.LOW,
    ): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = gene,
            isReportable = isReportable,
            isCancerAssociatedVariant = isCancerAssociatedVariant,
            driverLikelihood = driverLikelihood,
            extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = isBiallelic)
        )
    }
}