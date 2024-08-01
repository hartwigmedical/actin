package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IsEligibleForFirstLinePalliativeChemotherapyTest {

    private val alwaysPassMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
        every { evaluate(any()) } returns EvaluationFactory.pass("metastatic cancer")
    }
    private val alwaysUndeterminedMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
        every { evaluate(any()) } returns EvaluationFactory.undetermined("tumor stage unknown")
    }
    private val functionMetastaticCancer = IsEligibleForFirstLinePalliativeChemotherapy(alwaysPassMetastaticCancerEvaluation)
    private val functionUndeterminedMetastaticCancer =
        IsEligibleForFirstLinePalliativeChemotherapy(alwaysUndeterminedMetastaticCancerEvaluation)
    private val palliativeChemotherapy = patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.CHEMOTHERAPY, Intent.PALLIATIVE)
    private val palliativeTargetedTherapy =
        patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.TARGETED_THERAPY, Intent.PALLIATIVE)
    private val consolidationChemotherapy =
        patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.CHEMOTHERAPY, Intent.CONSOLIDATION)

    @Test
    fun `Should fail when no metastatic cancer (and previous palliative chemotherapy)`() {
        val alwaysFailsMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.fail("no metastatic cancer")
        }
        val function = IsEligibleForFirstLinePalliativeChemotherapy(alwaysFailsMetastaticCancerEvaluation)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(palliativeChemotherapy))
    }

    @Test
    fun `Should fail when metastatic cancer and previous palliative chemotherapy`() {
        assertEvaluation(EvaluationResult.FAIL, functionMetastaticCancer.evaluate(palliativeChemotherapy))
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and previous palliative targeted therapy`() {
        val result = functionMetastaticCancer.evaluate(palliativeTargetedTherapy)
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedGeneralMessages).containsExactly("Patient had palliative targeted therapy (hence may not be considered eligible for first line palliative chemotherapy)")
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and no previous palliative therapy`() {
        val result = functionMetastaticCancer.evaluate(consolidationChemotherapy)
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedGeneralMessages).containsExactly("Undetermined if considered eligible for first line palliative chemotherapy")
    }

    @Test
    fun `Should be undetermined when undetermined if patient has metastatic cancer and no previous palliative therapy`() {
        val result = functionUndeterminedMetastaticCancer.evaluate(consolidationChemotherapy)
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedGeneralMessages).containsExactly("Undetermined if metastatic cancer (hence may not be eligible for first line palliative chemotherapy)")
    }

    @Test
    fun `Should be undetermined when undetermined if patient has metastatic cancer and previous palliative targeted therapy`() {
        val result = functionUndeterminedMetastaticCancer.evaluate(palliativeTargetedTherapy)
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedGeneralMessages).containsExactly("Undetermined if metastatic cancer (hence may not be eligible for first line palliative chemotherapy)")
    }

    private fun patientRecordWithTreatmentWithCategoryAndIntent(category: TreatmentCategory, intent: Intent): PatientRecord {
        return TreatmentTestFactory.withTreatmentHistoryEntry(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "therapy",
                        category
                    )
                ), intents = setOf(intent)
            )
        )
    }
}