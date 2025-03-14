package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.algo.soc.StandardOfCareEvaluation
import com.hartwig.actin.algo.soc.StandardOfCareEvaluator
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
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

    private val standardOfCareEvaluator = mockk<StandardOfCareEvaluator>()
    private val standardOfCareEvaluatorFactory = mockk<StandardOfCareEvaluatorFactory> { every { create() } returns standardOfCareEvaluator }
    private val targetTreatment = treatment("PEMBROLIZUMAB", true)
    val function = IsEligibleForOnLabelTreatment(targetTreatment, standardOfCareEvaluatorFactory)
    private val colorectalCancerPatient = TumorTestFactory.withDoidAndSubLocation(DoidConstants.COLORECTAL_CANCER_DOID, "left")

    @Test
    fun `Should return undetermined for colorectal cancer patient eligible for on label treatment pembrolizumab`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false, setOf(eligibilityFunction)
        )
        val expectedSocTreatments = listOf(EvaluatedTreatment(treatmentCandidate, listOf(EvaluationFactory.pass("Has MSI"))))

        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(colorectalCancerPatient) } returns true
        every {
            standardOfCareEvaluator.standardOfCareEvaluatedTreatments(colorectalCancerPatient)
        } returns StandardOfCareEvaluation(expectedSocTreatments)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(colorectalCancerPatient))
    }

    @Test
    fun `Should fail for colorectal cancer patient ineligible for on label treatment pembrolizumab`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(colorectalCancerPatient) } returns true
        every {
            standardOfCareEvaluator.standardOfCareEvaluatedTreatments(colorectalCancerPatient)
        } returns StandardOfCareEvaluation(emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(colorectalCancerPatient))
    }

    @Test
    fun `Should return undetermined for tumor type CUP`() {
        standardOfCareCannotBeEvaluatedForPatient()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withTumorDetails(TumorDetails(primaryTumorLocation = "unknown", primaryTumorSubLocation = "CUP"))
            )
        )
    }

    @Test
    fun `Should warn for non colorectal cancer patient with target treatment already administered in history`() {
        standardOfCareCannotBeEvaluatedForPatient()
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry(setOf(targetTreatment, treatment("other", true)))))
            )
        )
    }

    @Test
    fun `Should return undetermined for non colorectal cancer patient with empty treatment list`() {
        standardOfCareCannotBeEvaluatedForPatient()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should return undetermined for non colorectal cancer patient with non empty treatment list but not containing the specific treatment`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val treatments = listOf(treatmentHistoryEntry(setOf(treatment("test", true))))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(treatments)))
    }

    private fun standardOfCareCannotBeEvaluatedForPatient() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(any()) } returns false
        every { standardOfCareEvaluator.standardOfCareEvaluatedTreatments(any()) } returns StandardOfCareEvaluation(emptyList())
    }
}