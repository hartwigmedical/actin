package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
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
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.doid.TestDoidModelFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasExhaustedSOCTreatmentsTest {

    private val standardOfCareEvaluator = mockk<StandardOfCareEvaluator>()
    private val standardOfCareEvaluatorFactory =
        mockk<StandardOfCareEvaluatorFactory> { every { create() } returns standardOfCareEvaluator }
    private val function = HasExhaustedSOCTreatments(standardOfCareEvaluatorFactory, TestDoidModelFactory.createMinimalTestDoidModel())
    private val nonEmptyTreatmentList = listOf(
        EvaluatedTreatment(
            TreatmentCandidate(
                TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false,
                setOf(EligibilityFunction(EligibilityRule.MMR_DEFICIENT, emptyList()))
            ), listOf(EvaluationFactory.pass("Has MSI"))
        )
    )

    private val curativeNeoAdjuvantOrAdjuvant = setOf(Intent.CURATIVE, Intent.NEOADJUVANT, Intent.ADJUVANT)

    @Test
    fun `Should pass for patient with NSCLC and platinum doublet chemotherapy in treatment history`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        val platinumDoublet =
            DrugTreatment(
                name = "Carboplatin+Pemetrexed",
                drugs = setOf(
                    Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)),
                    Drug(name = "Pemetrexed", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTIMETABOLITE))
                )
            )
        val record = createHistoryWithNSCLCAndTreatmentWithIntents(platinumDoublet)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail for patient with NSCLC and platinum doublet chemotherapy in treatment history but intent is curative, neoadjuvant or adjuvant`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        val platinumDoublet =
            DrugTreatment(
                name = "Carboplatin+Pemetrexed",
                drugs = setOf(
                    Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)),
                    Drug(name = "Pemetrexed", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTIMETABOLITE))
                )
            )

        curativeNeoAdjuvantOrAdjuvant.forEach { intent ->
            val record = createHistoryWithNSCLCAndTreatmentWithIntents(
                platinumDoublet,
                intents = setOf(intent)
            )
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
        }
    }

    @Test
    fun `Should pass for patient with NSCLC and history entry with chemo-immuno or chemoradiation with undefined chemotherapy`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        val chemoradiation = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = listOf(
                TreatmentTestFactory.treatment("CHEMOTHERAPY", true, setOf(TreatmentCategory.CHEMOTHERAPY), emptySet()),
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

        listOf(chemoradiation, chemoradiationWithOther).forEach {
            assertEvaluation(
                EvaluationResult.PASS,
                function.evaluate(
                    TumorTestFactory.withDoids(setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID))
                        .copy(oncologicalHistory = listOf(it))
                )
            )
        }
    }

    @Test
    fun `Should fail for patient with NSCLC and history entry with chemo-immuno or chemoradiation with undefined chemotherapy but intent is curative, neoadjuvant or adjuvant`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)

        curativeNeoAdjuvantOrAdjuvant.forEach { intent ->
            val chemoradiation = TreatmentTestFactory.treatmentHistoryEntry(
                treatments = listOf(
                    TreatmentTestFactory.treatment("CHEMOTHERAPY", true, setOf(TreatmentCategory.CHEMOTHERAPY), emptySet()),
                    TreatmentTestFactory.treatment("RADIOTHERAPY", false, setOf(TreatmentCategory.RADIOTHERAPY), emptySet())
                ),
                intents = setOf(intent)
            )

            val chemoradiationWithOther = chemoradiation.copy(
                treatments = chemoradiation.treatments + TreatmentTestFactory.treatment(
                    "OTHER",
                    false,
                    setOf(TreatmentCategory.IMMUNOTHERAPY),
                    emptySet()
                )
            )

            listOf(chemoradiation, chemoradiationWithOther).forEach {
                assertEvaluation(
                    EvaluationResult.FAIL,
                    function.evaluate(
                        TumorTestFactory.withDoids(setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID))
                            .copy(oncologicalHistory = listOf(it))
                    )
                )
            }
        }
    }

    @Test
    fun `Should pass for patient with NSCLC and history entry with chemo-immuno with undefined chemotherapy`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        val record = createHistoryWithNSCLCAndTreatmentWithIntents(
            TreatmentTestFactory.drugTreatment("CHEMOTHERAPY+IMMUNOTHERAPY", TreatmentCategory.CHEMOTHERAPY)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail for patient with NSCLC and history entry with chemo-immuno with undefined chemotherapy but intent is curative, neoadjuvant or adjuvant`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)

        curativeNeoAdjuvantOrAdjuvant.forEach { intent ->
            val record = createHistoryWithNSCLCAndTreatmentWithIntents(
                TreatmentTestFactory.drugTreatment("CHEMOTHERAPY+IMMUNOTHERAPY", TreatmentCategory.CHEMOTHERAPY), setOf(intent)
            )
            assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
        }
    }

    @Test
    fun `Should return undetermined for patient with NSCLC and history entry with undefined chemotherapy`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        val record = createHistoryWithNSCLCAndTreatmentWithIntents(
            TreatmentTestFactory.drugTreatment("CHEMOTHERAPY", TreatmentCategory.CHEMOTHERAPY)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail for patient with NSCLC with other treatment in treatment history`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        val treatment =
            TreatmentTestFactory.drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
        val record = createHistoryWithNSCLCAndTreatmentWithIntents(treatment)
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings())
            .containsExactly("Has not exhausted SOC (has not received platinum doublet in metastatic setting)")
    }

    @Test
    fun `Should fail for patient with NSCLC with empty treatment history`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        val record = createHistoryWithNSCLCAndTreatmentWithIntents(null)
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings())
            .containsExactly("Has not exhausted SOC (has not received platinum doublet in metastatic setting)")
    }

    @Test
    fun `Should return undetermined for empty treatment list when SOC cannot be evaluated`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should return not evaluated for non empty treatment list when SOC cannot be evaluated`() {
        setStandardOfCareCanBeEvaluatedForPatient(false)
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(nonEmptyTreatmentList)
        val treatments = listOf(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
    }

    private fun setStandardOfCareCanBeEvaluatedForPatient(canBeEvaluated: Boolean) {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns canBeEvaluated
    }

    @Test
    fun `Should pass when patient is known to have exhausted SOC`() {
        setStandardOfCareCanBeEvaluatedForPatient(true)
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(emptyList())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail when patient is known to have not exhausted SOC`() {
        setStandardOfCareCanBeEvaluatedForPatient(true)
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(nonEmptyTreatmentList)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
        assertThat(function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())).failMessagesStrings())
            .containsExactly("Has not exhausted SOC (remaining options: Pembrolizumab)")
    }

    @Test
    fun `Should warn when SOC evaluation is missing some molecular results`() {
        setStandardOfCareCanBeEvaluatedForPatient(true)
        val treatments = listOf(
            EvaluatedTreatment(
                TreatmentCandidate(
                    TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false,
                    setOf(EligibilityFunction(EligibilityRule.MMR_DEFICIENT, emptyList()))
                ),
                listOf(EvaluationFactory.undetermined("Cannot determine if MSI", isMissingMolecularResultForEvaluation = true))
            )
        )
        every { standardOfCareEvaluator.evaluateRequiredTreatments(any()) } returns StandardOfCareEvaluation(treatments)

        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnMessagesStrings())
            .containsExactly("Has potentially not exhausted SOC (Pembrolizumab) but some corresponding molecular results are missing")
        assertThat(evaluation.isMissingMolecularResultForEvaluation).isTrue
    }

    private fun createHistoryWithNSCLCAndTreatmentWithIntents(drugTreatment: Treatment?, intents: Set<Intent>? = null): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return base.copy(
            tumor = base.tumor.copy(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)),
            oncologicalHistory = if (drugTreatment != null) {
                listOf(
                    TreatmentTestFactory.treatmentHistoryEntry(listOf(drugTreatment), intents = intents)
                )
            } else emptyList()
        )
    }
}