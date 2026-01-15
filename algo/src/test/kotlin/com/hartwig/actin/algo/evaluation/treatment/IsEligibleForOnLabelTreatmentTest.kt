package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.algo.soc.StandardOfCareEvaluation
import com.hartwig.actin.algo.soc.StandardOfCareEvaluator
import com.hartwig.actin.algo.soc.StandardOfCareEvaluatorFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.doid.TestDoidModelFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.time.LocalDate

private val MIN_DATE = LocalDate.of(2020, 6, 6)
private val OSIMERTINIB = treatment("OSIMERTINIB", true)
private val PEMBROLIZUMAB = treatment("PEMBROLIZUMAB", true)

class IsEligibleForOnLabelTreatmentTest {

    private val standardOfCareEvaluator = mockk<StandardOfCareEvaluator>()
    private val standardOfCareEvaluatorFactory =
        mockk<StandardOfCareEvaluatorFactory> { every { create() } returns standardOfCareEvaluator }
    private val targetTreatment = treatment("TREATMENT", true)
    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function =
        IsEligibleForOnLabelTreatment(targetTreatment, standardOfCareEvaluatorFactory, doidModel, MIN_DATE)
    private val functionEvaluatingOsimertinib =
        IsEligibleForOnLabelTreatment(
            OSIMERTINIB,
            standardOfCareEvaluatorFactory,
            doidModel,
            MIN_DATE
        )
    private val functionEvaluatingPembrolizumab = IsEligibleForOnLabelTreatment(
        PEMBROLIZUMAB,
        standardOfCareEvaluatorFactory,
        doidModel,
        MIN_DATE
    )

    private val colorectalCancerPatient = TumorTestFactory.withDoidAndName(DoidConstants.COLORECTAL_CANCER_DOID, "left")
    private val nsclcTumor = TumorDetails(doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID))

    @Test
    fun `Should pass for NSCLC patient eligible for on label treatment osimertinib based on EGFR exon19 deletion`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "EGFR",
                isReportable = true,
                type = VariantType.DELETE,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(affectedExon = 19),
                clonalLikelihood = 1.0,
                driverLikelihood = DriverLikelihood.HIGH,
                proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                isCancerAssociatedVariant = true
            )
        ).copy(tumor = nsclcTumor)
        assertEvaluation(EvaluationResult.PASS, functionEvaluatingOsimertinib.evaluate(record))
    }

    @Test
    fun `Should fail for NSCLC patient not eligible for on label treatment osimertinib`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "EGFR",
                isReportable = true,
                type = VariantType.INSERT,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(affectedExon = 20),
                driverLikelihood = DriverLikelihood.HIGH
            )
        ).copy(tumor = nsclcTumor)
        assertEvaluation(EvaluationResult.FAIL, functionEvaluatingOsimertinib.evaluate(record))
    }

    @Test
    fun `Should fail for NSCLC patient who previously progressed on osimertinib`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    treatments = setOf(OSIMERTINIB),
                    stopReason = StopReason.PROGRESSIVE_DISEASE
                )
            )
        ).copy(tumor = nsclcTumor)
        assertEvaluation(EvaluationResult.FAIL, functionEvaluatingOsimertinib.evaluate(record))
    }

    @Test
    fun `Should fail for NSCLC patient who recently received osimertinib`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    treatments = setOf(OSIMERTINIB),
                    stopYear = MIN_DATE.year,
                    stopMonth = MIN_DATE.monthValue + 3
                )
            )
        ).copy(tumor = nsclcTumor)
        assertEvaluation(EvaluationResult.FAIL, functionEvaluatingOsimertinib.evaluate(record))
    }

    @Test
    fun `Should pass for NSCLC patient eligible for on label treatment osimertinib based on EGFR T790M mutation and prior TKI`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "EGFR",
                isReportable = true,
                type = VariantType.SNV,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(hgvsProteinImpact = "T790M"),
                clonalLikelihood = 1.0,
                driverLikelihood = DriverLikelihood.HIGH
            )
        ).copy(
            tumor = nsclcTumor,
            oncologicalHistory = listOf(
                treatmentHistoryEntry(
                    treatments = setOf(
                        TreatmentTestFactory.drugTreatment(
                            "TKI gen 2",
                            TreatmentCategory.TARGETED_THERAPY,
                            setOf(DrugType.TYROSINE_KINASE_INHIBITOR_GEN_2)
                        )
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, functionEvaluatingOsimertinib.evaluate(record))
    }

    @Test
    fun `Should pass for treatment naive NSCLC patient eligible for on label treatment pembrolizumab with PD-L1 TPS above 50 and no driver events in EGFR and ALK`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = nsclcTumor,
            ihcTests = listOf(IhcTest(item = "PD-L1", measure = "TPS", scoreValue = 55.0, scoreValueUnit = "%")),
            oncologicalHistory = emptyList()
        )
        assertEvaluation(EvaluationResult.PASS, functionEvaluatingPembrolizumab.evaluate(record))
    }

    @Test
    fun `Should fail for treatment naive NSCLC patient not eligible for on label treatment pembrolizumab based on EGFR driver`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "EGFR",
                event = "EGFR L858R",
                isCancerAssociatedVariant = true,
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH
            )
        ).copy(tumor = nsclcTumor, oncologicalHistory = emptyList())
        assertEvaluation(EvaluationResult.FAIL, functionEvaluatingPembrolizumab.evaluate(record))
    }

    @Test
    fun `Should return undetermined for treatment naive NSCLC patient with uncertain eligibility for on label treatment pembrolizumab based on PD-L1 TPS status below 50 and no EGFR or ALK driver`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = nsclcTumor,
            ihcTests = listOf(IhcTest(item = "PD-L1", measure = "TPS", scoreValue = 30.0, scoreValueUnit = "%")),
            oncologicalHistory = emptyList()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingPembrolizumab.evaluate(record))
    }

    @Test
    fun `Should return undetermined for non treatment naive NSCLC patient with uncertain eligibility for on label treatment pembrolizumab`() {
        standardOfCareCannotBeEvaluatedForPatient()
        val record =
            withTreatmentHistory(listOf(treatmentHistoryEntry(setOf(targetTreatment, treatment("other", true))))).copy(tumor = nsclcTumor)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingPembrolizumab.evaluate(record))
    }

    @Test
    fun `Should return undetermined for colorectal cancer patient eligible for on label treatment pembrolizumab`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MMR_DEFICIENT.name, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY), false, setOf(eligibilityFunction)
        )
        val expectedSocTreatments = listOf(EvaluatedTreatment(treatmentCandidate, listOf(EvaluationFactory.pass("Has MSI"))))

        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(colorectalCancerPatient) } returns true
        every {
            standardOfCareEvaluator.standardOfCareEvaluatedTreatments(colorectalCancerPatient)
        } returns StandardOfCareEvaluation(expectedSocTreatments)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionEvaluatingPembrolizumab.evaluate(colorectalCancerPatient))
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
                TumorTestFactory.withTumorDetails(TumorDetails(name = "Unknown (CUP)"))
            )
        )
    }

    @Test
    fun `Should warn for non colorectal cancer patient with target treatment already administered in history`() {
        standardOfCareCannotBeEvaluatedForPatient()
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                withTreatmentHistory(listOf(treatmentHistoryEntry(setOf(targetTreatment, treatment("other", true)))))
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