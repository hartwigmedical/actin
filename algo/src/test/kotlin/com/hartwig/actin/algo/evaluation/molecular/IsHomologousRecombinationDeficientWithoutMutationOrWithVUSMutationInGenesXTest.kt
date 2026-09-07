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
import org.junit.jupiter.api.Test

class IsHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesXTest {

    private val genesToFind = setOf("BRCA1", "BRCA2")
    private val function = IsHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesX(genesToFind)

    @Test
    fun `Should fail when HRD with deletion of BRCA1`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDeletion(
                    true,
                    TestCopyNumberFactory.createMinimal()
                        .copy(
                            canonicalImpact = TestTranscriptCopyNumberImpactFactory
                                .createTranscriptCopyNumberImpact(CopyNumberType.PARTIAL_DEL),
                            gene = "BRCA1",
                            driverLikelihood = DriverLikelihood.HIGH
                        )
                )
            ),
            "Tumor is HRD with BRCA1 deletion or partial deletion"
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
            ),
            "Tumor is HRD with BRCA1 cancer-associated variant"
        )
    }

    @Test
    fun `Should fail when no HRD`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHomologousRecombinationAndVariant(false, hrdVariant())),
            "Tumor is not HRD"
        )
    }

    @Test
    fun `Should be undetermined when HRD status unknown and no reportable drivers in HR genes`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withVariant(hrdVariant())),
            "Unknown HRD status"
        )
    }

    @Test
    fun `Should be undetermined when HRD status unknown but with drivers in HR genes`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withVariant(hrdVariant(isReportable = true))),
            "Unknown HRD status but non-biallelic drivers in HR genes"
        )
    }

    @Test
    fun `Should warn when HRD and only a non reportable mutation in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHomologousRecombinationAndVariant(true, hrdVariant())),
            "Tumor is HRD but without drivers in HR genes"
        )
    }

    @Test
    fun `Should warn when HRD and non-cancer-associated variant biallelic high driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true, isBiallelic = true, driverLikelihood = DriverLikelihood.HIGH)
                )
            ),
            "Tumor is HRD with non-cancer-associated variant biallelic high driver(s) in BRCA1 which could be pathogenic"
        )
    }

    @Test
    fun `Should warn when HRD and non-cancer-associated variant biallelic low driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true, isBiallelic = true)
                )
            ),
            "Tumor is HRD with non-cancer-associated variant biallelic non-high driver(s) in BRCA1 which could be pathogenic"
        )
    }

    @Test
    fun `Should warn when HRD and non-cancer-associated variant non-biallelic high driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true, driverLikelihood = DriverLikelihood.HIGH)
                )
            ),
            "Tumor is HRD with non-cancer-associated variant non-biallelic high driver(s) in BRCA1 which could be pathogenic"
        )
    }

    @Test
    fun `Should warn when HRD and non-cancer-associated variant non-biallelic low driver in BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(
                    true,
                    hrdVariant(isReportable = true)
                )
            ),
            "Tumor is HRD with non-cancer-associated variant non-biallelic non-high driver(s) in BRCA1 which could be pathogenic"
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
        assertEvaluation(EvaluationResult.WARN, result, "Tumor is HRD but without drivers in HR genes")
    }

    @Test
    fun `Should warn when HRD and disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal()
                        .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true)
                )
            ),
            "Tumor is HRD with non-homozygous disruption in BRCA1 which could be pathogenic"
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
            ),
            "Tumor is HRD but with only non-biallelic drivers in HR genes"
        )
    }

    @Test
    fun `Should warn when HRD and only a non-homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal()
                        .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true)
                )
            ),
            "Tumor is HRD with non-homozygous disruption in BRCA1 which could be pathogenic"
        )
    }

    @Test
    fun `Should warn when HRD and no detected drivers in HR genes`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariant(true, hrdVariant())
            ),
            "Tumor is HRD but without drivers in HR genes"
        )
    }

    @Test
    fun `Should warn when HRD and biallelic RAD51C cancer-associated variant and non-homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndVariantAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal()
                        .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true),
                    hrdVariant("RAD51C", true, true, true)
                )
            ),
            "Tumor is HRD with non-homozygous disruption in BRCA1 which could be pathogenic"
        )
    }

    @Test
    fun `Should warn when HRD and homozygous disruption of BRCA1`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRecombinationAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = "BRCA1")
                )
            ),
            "Tumor is HRD with homozygous disruption in BRCA1 which could be pathogenic"
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
            ),
            "Tumor is HRD without any variants in BRCA1 and BRCA2"
        )
    }

    @Test
    fun `Should warn when HRD and biallelic non-cancer-associated variant BRCA1 and non-homozygous disruption of BRCA1`() {
        val result = function.evaluate(
            MolecularTestFactory.withHomologousRecombinationAndVariantAndDisruption(
                true,
                TestDisruptionFactory.createMinimal()
                    .copy(gene = "BRCA1", driverLikelihood = DriverLikelihood.HIGH, isReportable = true),
                hrdVariant("BRCA1", true, true, false)
            )
        )
        assertEvaluation(
            EvaluationResult.WARN,
            result,
            "Tumor is HRD with non-cancer-associated variant biallelic non-high driver(s) in BRCA1 and non-homozygous disruption in BRCA1 which could be pathogenic"
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
            isBiallelic = isBiallelic
        )
    }
}