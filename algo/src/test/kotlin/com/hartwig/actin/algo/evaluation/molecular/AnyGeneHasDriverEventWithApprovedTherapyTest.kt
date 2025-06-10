package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.doid.TestDoidModelFactory.createMinimalTestDoidModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val CORRECT_GENE = "BRAF"
private const val CORRECT_PROTEIN_IMPACT = "V600E"
private const val INCORRECT_GENE = "INCORRECT"
private val CORRECT_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = CORRECT_GENE,
    canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(hgvsProteinImpact = CORRECT_PROTEIN_IMPACT),
    isReportable = true
)
private val INCORRECT_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = INCORRECT_GENE,
    canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(hgvsProteinImpact = "INCORRECT"),
    isReportable = true
)

class AnyGeneHasDriverEventWithApprovedTherapyTest {
    private val resources = RuleMappingResourcesTestFactory.create()
    private val function = AnyGeneHasDriverEventWithApprovedTherapy(
        setOf(CORRECT_GENE), createMinimalTestDoidModel(), EvaluationFunctionFactory.create(resources)
    )
    private val functionRequestingInvalidGenes = AnyGeneHasDriverEventWithApprovedTherapy(
        setOf(CORRECT_GENE, INCORRECT_GENE, "Other"),
        createMinimalTestDoidModel(),
        EvaluationFunctionFactory.create(resources)
    )

    @Test
    fun `Should pass if tumor type is NSCLC and correct variant present`() {
        val record =
            MolecularTestFactory.withVariant(CORRECT_VARIANT)
                .copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail if tumor type is NSCLC cancer but correct variant not present`() {
        val record =
            MolecularTestFactory.withVariant(INCORRECT_VARIANT)
                .copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should be undetermined if tumor type is NSCLC but requested gene cannot be evaluated`() {
        val record = MolecularTestFactory.withVariant(INCORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)))

        assertEvaluation(EvaluationResult.UNDETERMINED, functionRequestingInvalidGenes.evaluate(record))
        assertThat(functionRequestingInvalidGenes.evaluate(record).undeterminedMessages).containsExactly("Possible presence of driver events for gene(s) INCORRECT and Other could not be determined")
    }

    @Test
    fun `Should pass if tumor type is NSCLC cancer and gene is requested that cannot be evaluated but function also requests gene that is evaluated and pass`() {
        val record = MolecularTestFactory.withVariant(CORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)))

        assertEvaluation(EvaluationResult.PASS, functionRequestingInvalidGenes.evaluate(record))
    }

    @Test
    fun `Should pass if tumor type is colorectal cancer and correct variant present`() {
        val record = MolecularTestFactory.withVariant(CORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail if tumor type is colorectal cancer but correct variant not present`() {
        val record = MolecularTestFactory.withVariant(INCORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if tumor type is not lung or CRC`() {
        val record = MolecularTestFactory.withVariant(CORRECT_VARIANT)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if tumor type is neuroendocrine colorectal cancer `() {
        val record = MolecularTestFactory.withVariant(CORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.RECTUM_NEUROENDOCRINE_NEOPLASM_DOID)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail when no molecular data present`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        )
    }
}