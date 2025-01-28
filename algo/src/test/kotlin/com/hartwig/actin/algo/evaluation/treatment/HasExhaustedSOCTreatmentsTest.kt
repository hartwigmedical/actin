package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.soc.StandardOfCareEvaluation
import com.hartwig.actin.algo.soc.StandardOfCareEvaluator
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.doid.TestDoidModelFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {

    private val standardOfCareEvaluator = mockk<StandardOfCareEvaluator>()
    private val standardOfCareEvaluatorFactory = mockk<StandardOfCareEvaluatorFactory> { every { create() } returns standardOfCareEvaluator }
    private val function = HasExhaustedSOCTreatments(standardOfCareEvaluatorFactory, TestDoidModelFactory.createMinimalTestDoidModel())
    private val nonEmptyTreatmentList = listOf(
        EvaluatedTreatment(
            TreatmentCandidate(
                TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false,
                setOf(EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList()))
            ), listOf(EvaluationFactory.pass("Has MSI"))
        )
    )

    @Test
    fun `Should pass for patient with NSCLC and platinum doublet chemotherapy in treatment history`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val platinumDoublet =
            DrugTreatment(
                name = "Carboplatin+Pemetrexed",
                drugs = setOf(
                    Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)),
                    Drug(name = "Pemetrexed", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTIMETABOLITE))
                )
            )
        val record = createHistoryWithNSCLCAndTreatment(platinumDoublet)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass for patient with NSCLC and history entry with treatment names CHEMOTHERAPY and RADIOTHERAPY`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val chemoradiation =
            TreatmentTestFactory.treatmentHistoryEntry(
                listOf(
                    DrugTreatment(
                        name = "CHEMOTHERAPY",
                        drugs = setOf(
                            Drug(name = "Chemo", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet()),
                        )
                    ),
                    TreatmentTestFactory.treatment("RADIOTHERAPY", false, setOf(TreatmentCategory.RADIOTHERAPY), emptySet())
                )
            )

        val chemoradiationWithOther = chemoradiation.copy(
            treatments = chemoradiation.treatments + TreatmentTestFactory.treatment(
                "OTHER",
                false,
                setOf(TreatmentCategory.IMMUNOTHERAPY),
                emptySet()
            )
        )

        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        listOf(chemoradiation, chemoradiationWithOther).forEach {
            val record = base.copy(
                tumor = base.tumor.copy(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)),
                oncologicalHistory = listOf(it)
            )
            assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
        }
    }

    @Test
    fun `Should fail for patient with NSCLC with other treatment in treatment history`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val treatment =
            TreatmentTestFactory.drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
        val record = createHistoryWithNSCLCAndTreatment(treatment)
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("Has not exhausted SOC (at least platinum doublet remaining)")
    }

    @Test
    fun `Should fail for patient with NSCLC with empty treatment history`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        val record = createHistoryWithNSCLCAndTreatment(null)
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly("Has not exhausted SOC (at least platinum doublet remaining)")
    }

    @Test
    fun `Should return undetermined for empty treatment list when SOC cannot be evaluated`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should return not evaluated for non empty treatment list when SOC cannot be evaluated`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(nonEmptyTreatmentList)
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should pass when patient is known to have exhausted SOC`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns true
        every { standardOfCareEvaluator.patientHasExhaustedStandardOfCare(any()) } returns true
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(emptyList())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient is known to have not exhausted SOC`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns true
        every { standardOfCareEvaluator.patientHasExhaustedStandardOfCare(any()) } returns false
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(nonEmptyTreatmentList)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
        assertThat(function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())).failMessages)
            .containsExactly("Has not exhausted SOC (remaining options: pembrolizumab)")
    }

    private fun createHistoryWithNSCLCAndTreatment(drugTreatment: Treatment?): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return base.copy(
            tumor = base.tumor.copy(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)),
            oncologicalHistory = if (drugTreatment != null) {
                listOf(
                    TreatmentTestFactory.treatmentHistoryEntry(listOf(drugTreatment))
                )
            } else emptyList()
        )
    }
}