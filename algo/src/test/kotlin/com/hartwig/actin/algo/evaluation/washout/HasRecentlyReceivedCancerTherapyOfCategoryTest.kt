package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val REFERENCE_DATE = LocalDate.of(2020, 6, 6)
private val INTERPRETER = WashoutTestFactory.activeFromDate(REFERENCE_DATE)

class HasRecentlyReceivedCancerTherapyOfCategoryTest {

    private val function = HasRecentlyReceivedCancerTherapyOfCategory(
        mapOf(
            "Chemotherapy" to setOf(AtcLevel(code = "category to find", name = "")),
            "Monoclonal antibodies and antibody drug conjugates" to setOf(AtcLevel(code = "second category to find", name = ""))
        ),
        mapOf("categories to ignore" to setOf(AtcLevel(code = "category to ignore", name = ""))),
        INTERPRETER,
        REFERENCE_DATE
    )

    @Test
    fun `Should fail when no medications`() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when medication has the wrong category`() {
        val atc = AtcTestFactory.atcClassification("wrong category")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when medication has right category and old date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.minusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication has right category and recent date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication has right category and old date but treatment history entry has correct category and date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.minusDays(1)))
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                startYear = REFERENCE_DATE.year,
                startMonth = REFERENCE_DATE.plusMonths(1).monthValue,
                stopYear = REFERENCE_DATE.year,
                stopMonth = REFERENCE_DATE.plusMonths(2).monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, medications))
        )
    }

    @Test
    fun `Should pass when medication has right category and old date but treatment history entry has correct type and date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.minusDays(1)))
        val treatments = TreatmentTestFactory.drugTreatment(
            "PARP inhibitor",
            category = TreatmentCategory.IMMUNOTHERAPY,
            types = setOf(DrugType.MONOCLONAL_ANTIBODY_IMMUNOTHERAPY)
        )
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                startYear = REFERENCE_DATE.year,
                startMonth = REFERENCE_DATE.plusMonths(1).monthValue,
                stopYear = REFERENCE_DATE.year,
                stopMonth = REFERENCE_DATE.plusMonths(2).monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, medications))
        )
    }

    @Test
    fun `Should be undetermined when treatment history entry has correct category but inconclusive date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.minusDays(1)))
        val treatments = TreatmentTestFactory.drugTreatment(
            "PARP inhibitor",
            category = TreatmentCategory.IMMUNOTHERAPY,
            types = setOf(DrugType.MONOCLONAL_ANTIBODY_IMMUNOTHERAPY)
        )
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, medications))
        )
    }

    @Test
    fun `Should be undetermined when treatment history entry is a trial`() {
        val treatments = TreatmentTestFactory.treatment("trial", true)
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                startYear = REFERENCE_DATE.year,
                startMonth = REFERENCE_DATE.plusMonths(1).monthValue,
                stopYear = REFERENCE_DATE.year,
                stopMonth = REFERENCE_DATE.plusMonths(2).monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should return undetermined when medication is trial medication`() {
        val medications = listOf(WashoutTestFactory.medication(isTrialMedication = true, stopDate = REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should be undetermined if medication is not provided and no matching treatment history entry`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }

    @Test
    fun `Should pass if medication is not provided but matching treatment history entry`() {
        val treatments = TreatmentTestFactory.drugTreatment(
            "PARP inhibitor",
            category = TreatmentCategory.IMMUNOTHERAPY,
            types = setOf(DrugType.MONOCLONAL_ANTIBODY_IMMUNOTHERAPY)
        )
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                startYear = REFERENCE_DATE.year,
                startMonth = REFERENCE_DATE.plusMonths(1).monthValue,
                stopYear = REFERENCE_DATE.year,
                stopMonth = REFERENCE_DATE.plusMonths(2).monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, null))
        )
    }
}