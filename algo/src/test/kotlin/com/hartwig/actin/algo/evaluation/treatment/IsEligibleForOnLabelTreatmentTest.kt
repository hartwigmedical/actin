package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.algo.soc.RecommendationEngine
import com.hartwig.actin.algo.soc.RecommendationEngineFactory
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class IsEligibleForOnLabelTreatmentTest {

    private val recommendationEngine = mockk<RecommendationEngine>()
    private val recommendationEngineFactory = mockk<RecommendationEngineFactory> { every { create() } returns recommendationEngine }
    private val targetTreatment = treatment("PEMBROLIZUMAB", true)
    val function = IsEligibleForOnLabelTreatment(targetTreatment, recommendationEngineFactory)
    private val colorectalCancerPatient = TumorTestFactory.withDoidAndSubLocation(DoidConstants.COLORECTAL_CANCER_DOID, "left")

    @Test
    fun `Should return undetermined for colorectal cancer patient eligible for on label treatment pembrolizumab`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false, setOf(eligibilityFunction)
        )
        val expectedSocTreatments = listOf(EvaluatedTreatment(treatmentCandidate, listOf(EvaluationFactory.pass("Has MSI"))))

        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(colorectalCancerPatient) } returns true
        every { recommendationEngine.standardOfCareEvaluatedTreatments(colorectalCancerPatient) } returns expectedSocTreatments
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                colorectalCancerPatient
            )
        )
    }

    @Test
    fun `Should fail for colorectal cancer patient ineligible for on label treatment pembrolizumab`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(colorectalCancerPatient) } returns true
        every { recommendationEngine.standardOfCareEvaluatedTreatments(colorectalCancerPatient) } returns emptyList()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                colorectalCancerPatient
            )
        )
    }

    @Test
    fun `Should return undetermined for tumor type CUP`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { recommendationEngine.standardOfCareEvaluatedTreatments(any()) } returns emptyList()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withTumorDetails(TumorDetails(primaryTumorLocation = "unknown", primaryTumorSubLocation = "CUP"))
            )
        )
    }

    @Test
    fun `Should return undetermined for non colorectal cancer patient with empty treatment list`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { recommendationEngine.standardOfCareEvaluatedTreatments(any()) } returns emptyList()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should fail for non colorectal cancer patient with target treatment already administered in history`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { recommendationEngine.standardOfCareEvaluatedTreatments(any()) } returns emptyList()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry(setOf(targetTreatment)))))
        )
    }

    @Test
    fun `Should return not evaluated for non colorectal cancer patient with non empty treatment list`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { recommendationEngine.standardOfCareEvaluatedTreatments(any()) } returns emptyList()
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("test", true))))
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(withTreatmentHistory(treatments))
        )
    }
}