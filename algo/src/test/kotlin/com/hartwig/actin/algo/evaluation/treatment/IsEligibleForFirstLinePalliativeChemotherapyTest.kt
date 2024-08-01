package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TumorStage
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

    @Test
    fun `Should fail when no metastatic cancer`() {
        val alwaysFailsMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.fail("no metastatic cancer")
        }
        val function = IsEligibleForFirstLinePalliativeChemotherapy(alwaysFailsMetastaticCancerEvaluation)
        val patientRecord = TumorTestFactory.withTumorStage(TumorStage.I)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail when metastatic cancer and previous palliative chemotherapy`() {
        val patientRecord = patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.CHEMOTHERAPY, Intent.PALLIATIVE)
        assertEvaluation(EvaluationResult.FAIL, functionMetastaticCancer.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and previous palliative targeted therapy`() {
        val patientRecord = patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.TARGETED_THERAPY, Intent.PALLIATIVE)
        val result = functionMetastaticCancer.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedGeneralMessages).containsExactly("Patient had palliative targeted therapy (hence may not be eligible for first line palliative chemotherapy)")
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and no previous palliative therapy`() {
        val patientRecord = patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.CHEMOTHERAPY, Intent.CONSOLIDATION)
        val result = functionMetastaticCancer.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedGeneralMessages).containsExactly("Undetermined eligibility for first line palliative chemotherapy")
    }

    @Test
    fun `Should be undetermined when undetermined if patient has metastatic cancer and no previous palliative therapy`() {
        val patientRecord = patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.CHEMOTHERAPY, Intent.CONSOLIDATION)
        val result = functionUndeterminedMetastaticCancer.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedGeneralMessages).containsExactly("Undetermined if metastatic cancer (hence may not be eligible for first line palliative chemotherapy)")
    }

    @Test
    fun `Should be undetermined when undetermined if patient has metastatic cancer and previous palliative targeted therapy`() {
        val patientRecord = patientRecordWithTreatmentWithCategoryAndIntent(TreatmentCategory.TARGETED_THERAPY, Intent.PALLIATIVE)
        val result = functionUndeterminedMetastaticCancer.evaluate(patientRecord)
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