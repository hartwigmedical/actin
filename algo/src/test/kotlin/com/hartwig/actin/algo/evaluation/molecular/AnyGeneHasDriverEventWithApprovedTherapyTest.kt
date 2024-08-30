package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.doid.TestDoidModelFactory.createMinimalTestDoidModel
import org.junit.Test

private const val CORRECT_GENE = "BRAF"
private const val CORRECT_PROTEIN_IMPACT = "V600E"
private val CORRECT_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = CORRECT_GENE,
    canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(hgvsProteinImpact = CORRECT_PROTEIN_IMPACT),
    isReportable = true
)
private val INCORRECT_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = "INCORRECT",
    canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(hgvsProteinImpact = "INCORRECT"),
    isReportable = true
)

class AnyGeneHasDriverEventWithApprovedTherapyTest {
    private val resources = RuleMappingResourcesTestFactory.create()
    private val function = AnyGeneHasDriverEventWithApprovedTherapy(
        listOf(CORRECT_GENE), createMinimalTestDoidModel(), EvaluationFunctionFactory.create(resources)
    )

    @Test
    fun `Should pass if tumor type is lung and correct variant present`() {
        val record =
            MolecularTestFactory.withVariant(CORRECT_VARIANT).copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_CANCER_DOID)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail if tumor type is lung cancer but correct variant not present`() {
        val record =
            MolecularTestFactory.withVariant(INCORRECT_VARIANT).copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_CANCER_DOID)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should pass if tumor type is colorectal cancer and correct variant present`() {
        val record = MolecularTestFactory.withVariant(CORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail if tumor type is colorectal cancer but correct variant not present`() {
        val record = MolecularTestFactory.withVariant(INCORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.COLORECTAL_CANCER_DOID)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if tumor type is not lung or CRC`() {
        val record = MolecularTestFactory.withVariant(CORRECT_VARIANT)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if tumor type is neuroendocrine colorectal cancer `() {
        val record = MolecularTestFactory.withVariant(CORRECT_VARIANT)
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.RECTUM_NEUROENDOCRINE_NEOPLASM_DOID)))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail when no molecular data present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        )
    }
}